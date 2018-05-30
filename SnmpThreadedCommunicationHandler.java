package com.viasat.nbn.snmp.core.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.snmp4j.PDU;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.viasat.nbn.snmp.common.LogConstants;
import com.viasat.nbn.snmp.core.framework.SnmpConnection;
import com.viasat.nbn.snmp.exception.SnmpManagerException;
import com.viasat.nbn.snmp.util.LoggerUtil;
import com.viasat.nbn.snmp.webservices.snmpmanager.schema.SnmpError;
import com.viasat.nbn.snmp.webservices.snmpmanager.schema.VarBind;

public class SnmpThreadedCommunicationHandler {

	private ThreadPoolTaskExecutor taskExecutor;

	private Integer maxVarBindsPerPdu;
	
	public SnmpThreadedCommunicationHandler() {
		
	}
	
	public SnmpThreadedCommunicationHandler(ThreadPoolTaskExecutor taskExecutor, int maxVarBindsPerPdu) {
		this.maxVarBindsPerPdu = maxVarBindsPerPdu;
		this.taskExecutor = taskExecutor;
	}
	
	/**
	 * this method uses TableUtils from SNMP4J to call getTable method with one
	 * column per thread.
	 * @param requestId 
	 * 
	 * @param snmpConnection
	 * @param oidList
	 * @param snmpError
	 * @return list of varbinds
	 * @throws SnmpManagerException
	 */
	public List<VarBind> getTableOIDValuesSplitMode(
			String requestId, final SnmpConnection snmpConnection, final List<String> oidList,
			SnmpError snmpError, boolean serialize) throws SnmpManagerException {
		final List<VarBind> returnVarbinds = Collections
				.synchronizedList(new ArrayList<VarBind>());
		// to keep SnmpErrors in order of occurrence
		final Set<SnmpError> snmpErrorSet = Collections
				.synchronizedSet(new LinkedHashSet<SnmpError>());
		final ExecutorService singleThreadPoolExecutorService = Executors
				.newSingleThreadExecutor();
		// need to keep track of tasks created by this request
		final List<Future<Boolean>> tasks = new ArrayList<Future<Boolean>>();
		if (serialize) {
			for (final String oid : oidList) {
				Callable<Boolean> task = getTaskForColumnOid(snmpConnection,
						returnVarbinds, oid, snmpErrorSet, requestId);
				Future<Boolean> future = singleThreadPoolExecutorService.submit(task);
				tasks.add(future);
			}			
		} else {
			for (final String oid : oidList) {
				Callable<Boolean> task = getTaskForColumnOid(snmpConnection,
						returnVarbinds, oid, snmpErrorSet, requestId);
				Future<Boolean> future = taskExecutor.submit(task);
				tasks.add(future);
			}
		}
		String msg = requestId + ":" + System.currentTimeMillis() + ":submitted all tasks to pool";
		LoggerUtil.setLog(getClass(), LogConstants.DEBUG, msg);
		// checking if all tasks created by this request are completed
		while (true) {
			boolean completed = true;
			for (Future<Boolean> future : tasks) {
				completed = completed & future.isDone();
			}
			if (completed) {
				break;
			}
		}
		handleSnmpErrors(snmpErrorSet, snmpError);
		return returnVarbinds;
	}

	/**
	 * The method creates a task to fetch the all the rows for columnar oid in a
	 * thread. 
	 * @param snmpConnection
	 * @param returnVarbinds
	 * @param oid
	 * @param snmpErrorList
	 * @param requestId 
	 * @return task
	 */
	protected Callable<Boolean> getTaskForColumnOid(
			final SnmpConnection snmpConnection,
			final List<VarBind> returnVarbinds, final String oid,
			final Set<SnmpError> snmpErrorList, final String requestId) {
		return new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				List<String> oneOidList = new ArrayList<String>();
				oneOidList.add(oid);
				try {
					long startTime = System.currentTimeMillis();
					String msg = requestId + ":" + startTime + ":started:" + Thread.currentThread().getName()
							+ " : for column oid " + oid;
					System.out.println(msg);
					LoggerUtil.setLog(getClass(), LogConstants.DEBUG, msg);
					SnmpError snmpError = new SnmpError();

					List<VarBind> vbs = SnmpCommunicationHandler
							.getTableOIDValues(snmpConnection, oneOidList,
									snmpError, maxVarBindsPerPdu, 1);
					
					returnVarbinds.addAll(vbs);
					snmpErrorList.add(snmpError);
					long endTime = System.currentTimeMillis();
					long elapsedTime = endTime - startTime;
					msg = requestId + ":" + endTime + ":" + Thread.currentThread().getName() + " : for column "
							+ oid + " took " + elapsedTime + " milliseconds";
					LoggerUtil.setLog(getClass(), LogConstants.DEBUG, msg);
				} catch (SnmpManagerException se) {
					// other tasks may run successfully hence ignore this
					String msg = requestId + ":" + Thread.currentThread().getName()
							+ "Exception occured while fetching rows for column oid "
							+ oid;
					LoggerUtil.setLog(getClass(), LogConstants.WARN, msg);
				}
				return Boolean.TRUE;
			}
		};
	}


	/**
	 * The method executes snmp get on oids if the size > 100. in case
	 * serialization is required it uses a pool with single thread 
	 * @param requestId 
	 * @param snmpConnection
	 * @param oidList
	 * @param snmpError
	 * @param serialize
	 * @return
	 */
	public List<VarBind> getSplitOidValuesWithAsynchCalls(String requestId, List<String> oidList,
			SnmpError snmpError, boolean serialize,
			SnmpConnection snmpConnection, int pduType) {
		final List<VarBind> returnVarBinds = Collections
				.synchronizedList(new ArrayList<VarBind>());
		// keeping track of errors created in this request
		final Set<SnmpError> snmpErrorSet = new LinkedHashSet<SnmpError>();
		// keeping track of tasks created by this request
		final List<Future<SnmpMgrRespListener>> tasks = new ArrayList<Future<SnmpMgrRespListener>>();
		final ExecutorService singleThreadPoolExecutorService = Executors
				.newSingleThreadExecutor();
		final List<List<String>> subDividedList = divide(oidList,
				maxVarBindsPerPdu);
		if (!serialize) {
			int subId = 0;
			for (final List<String> oids : subDividedList) {
				Callable<SnmpMgrRespListener> task = getAsynchTaskForOids(
						snmpConnection, returnVarBinds, oids, pduType, requestId, ++subId);
				tasks.add(taskExecutor.submit(task));
			}
			String msg = requestId + ":" + System.currentTimeMillis() + ":submitted all tasks to multi thread pool";
			LoggerUtil.setLog(getClass(), LogConstants.DEBUG, msg);
			handleResponses(snmpError, returnVarBinds, snmpErrorSet, tasks);
		} else {
			int subId = 0;
			for (final List<String> oids : subDividedList) {
				Callable<SnmpMgrRespListener> task = getAsynchTaskForOids(
						snmpConnection, returnVarBinds, oids, pduType, requestId, ++subId);
				tasks.add(singleThreadPoolExecutorService.submit(task));
			}
			String msg = requestId + ":" + System.currentTimeMillis() + ":submitted all tasks to multi thread pool";
			LoggerUtil.setLog(getClass(), LogConstants.DEBUG, msg);
			handleResponses(snmpError, returnVarBinds, snmpErrorSet, tasks);
		}
		return returnVarBinds;
	}
	
	/**
	 * The method executes snmp get on oids if the size > 100. in case
	 * serialization is required it uses a pool with single thread 
	 * @param requestId 
	 * @param snmpConnection
	 * @param oidList
	 * @param snmpError
	 * @param serialize
	 * @return
	 */
	public List<VarBind> getSplitOidValuesWithSyncCall(String requestId, List<String> oidList,
			SnmpError snmpError, boolean serialize,
			SnmpConnection snmpConnection, int pduType, ExecutorService singleThreadTaskExecutor) {
		final List<VarBind> returnVarBinds = Collections
				.synchronizedList(new ArrayList<VarBind>());
		// keeping track of errors created in this request
		final Set<SnmpError> snmpErrorSet = new LinkedHashSet<SnmpError>();
		// keeping track of tasks created by this request
		final List<Future<Boolean>> tasks = new ArrayList<Future<Boolean>>();
		final List<List<String>> subDividedList = divide(oidList,
				maxVarBindsPerPdu);
		if (!serialize) {
			int subId = 0;
			for (final List<String> oids : subDividedList) {
				Callable<Boolean> task = getCallableTaskForOids(
						snmpConnection, returnVarBinds, oids, snmpErrorSet , pduType, requestId, ++subId);
				tasks.add(taskExecutor.submit(task));
			}
			String msg = requestId + ":" + System.currentTimeMillis() + ":submitted all tasks to multi thread pool";
			LoggerUtil.setLog(getClass(), LogConstants.DEBUG, msg);
		} else {
			int subId = 0;			
			for (final List<String> oids : subDividedList) {
				Callable<Boolean> task = getCallableTaskForOids(
						snmpConnection, returnVarBinds, oids, snmpErrorSet, pduType, requestId, ++subId);
				tasks.add(singleThreadTaskExecutor.submit(task));
			}
			String msg = requestId + ":" + System.currentTimeMillis() + ":submitted all tasks to single thread thread pool";
			LoggerUtil.setLog(getClass(), LogConstants.DEBUG, msg);
		}
		// check if tasks submitted to pool by this request are completed
		while (true) {
			boolean completed = true;
			for (Future<Boolean> future : tasks) {
				completed = completed & future.isDone();
			}			
			if (completed) {
				break;
			}
		}
		handleSnmpErrors(snmpErrorSet, snmpError);
		return returnVarBinds;
	}

	public List<VarBind> getSplitOidValuesWithExecutor(String requestId, List<String> oidList, SnmpError snmpError,
			boolean serialize, SnmpConnection snmpConnection, int pduType, ExecutorService singleThreadExecutorService) {
		final List<VarBind> returnVarBinds = Collections.synchronizedList(new ArrayList<VarBind>());
		// keeping track of errors created in this request
		final Set<SnmpError> snmpErrorSet = new LinkedHashSet<SnmpError>();
		// keeping track of tasks created by this request
		final List<Future<Boolean>> tasks = new ArrayList<Future<Boolean>>();
		int subId = 0;

		Callable<Boolean> task = getCallableTaskForOids(snmpConnection, returnVarBinds, oidList, snmpErrorSet, pduType,
				requestId, ++subId);
		tasks.add(singleThreadExecutorService.submit(task));

		String msg = requestId + ":" + System.currentTimeMillis() + ":submitted all tasks to single thread thread pool";
		LoggerUtil.setLog(getClass(), LogConstants.DEBUG, msg);

		// check if tasks submitted to pool by this request are completed
		while (true) {
			boolean completed = true;
			for (Future<Boolean> future : tasks) {
				completed = completed & future.isDone();
			}
			if (completed) {
				break;
			}
		}
		handleSnmpErrors(snmpErrorSet, snmpError);
		return returnVarBinds;
	}
	
	/**
	 * Handles responses from asynchronous snmp requests.
	 * Ensures all the responses have arrived and collates them
	 * @param snmpError
	 * @param returnVarBinds
	 * @param snmpErrorSet
	 * @param tasks
	 */
	protected void handleResponses(SnmpError snmpError,
			final List<VarBind> returnVarBinds,
			final Set<SnmpError> snmpErrorSet,
			final List<Future<SnmpMgrRespListener>> tasks) {
		// check if tasks submitted to pool by this request are completed
		while (true) {
			boolean completed = true;
			for (Future<SnmpMgrRespListener> future : tasks) {
				completed = completed & future.isDone();
			}			
			if (completed) {
				break;
			}
		}
		// check if all asynchronous responses have arrived
		while (true) {
			boolean allResponsesReceived = true;
			for (Future<SnmpMgrRespListener> future : tasks) {
				try {
					allResponsesReceived = allResponsesReceived
							& future.get().isDone();
				} catch (Exception e) {
					LoggerUtil.setLog(getClass(), LogConstants.ERROR,
							"Exception happened while retrieving future object from task");
					LoggerUtil.setLog(getClass(), LogConstants.ERROR,
							e.getMessage());
				}
			}
			if (allResponsesReceived) { 
				// all responses arrived, now consolidate
				for (Future<SnmpMgrRespListener> future : tasks) {
					try {
						SnmpMgrRespListener responseListener = future.get();
						if (!responseListener.isErrored()
								&& responseListener.getSnmpError()
										.getErrorStatus() == 0) {
							returnVarBinds.addAll(responseListener
									.getVarBindList());
							snmpErrorSet.add(responseListener
									.getSnmpError());
						}
					} catch (Exception e) {
						LoggerUtil.setLog(getClass(), LogConstants.ERROR,"Exception happened while retrieving future object from task");
						LoggerUtil.setLog(getClass(), LogConstants.ERROR,
								e.getMessage());
					}
				}
				break;
			}
		}
		handleSnmpErrors(snmpErrorSet, snmpError);
	}

	/**
	 * The method returns a runnable task to get values for a list of oids using
	 * synchronous call to SNMP 4J
	 * 
	 * @param snmpConnection
	 * @param ret
	 * @param oids
	 * @param snmpErrorSet
	 * @return a task that performs get
	 */
	protected Callable<Boolean> getCallableTaskForOids(
			final SnmpConnection snmpConnection, final List<VarBind> ret,
			final List<String> oids, final Set<SnmpError> snmpErrorSet,
			final int pduType, final String requestId, final int subId) {
		return new Callable<Boolean>() {
			@Override
			public Boolean call() {
				try {
					long startTime = System.currentTimeMillis();
					String msg = startTime + ":" + requestId + ":started:"
							+ subId + ":" + Thread.currentThread().getName()
							+ " : for oids " + oids + " started";
					LoggerUtil.setLog(getClass(), LogConstants.DEBUG, msg);
					SnmpError snmpError = new SnmpError();
					List<VarBind> vbs = new ArrayList<VarBind>();
					if (pduType == PDU.GET) {
						vbs = SnmpCommunicationHandler.getOIDValues(
								snmpConnection, oids, snmpError, subId);
					} else {
						vbs = SnmpCommunicationHandler.getNextOIDValues(
								snmpConnection, oids, snmpError, subId);
					}
					ret.addAll(vbs);
					snmpErrorSet.add(snmpError);
					long endTime = System.currentTimeMillis();
					long elapsedTime = endTime - startTime;
					msg = endTime + ":" + requestId + ":" + subId + ":"
							+ Thread.currentThread().getName() + " : for oids "
							+ oids + " took " + elapsedTime + " milliseconds";
					LoggerUtil.setLog(getClass(), LogConstants.DEBUG, msg);
				} catch (SnmpManagerException se) {
					// other tasks may run successfully hence ignore this
					String msg = Thread.currentThread().getName()
							+ "Exception occured while fetching " + oids;
					LoggerUtil.setLog(getClass(), LogConstants.WARN, msg);
				}
				return Boolean.TRUE;
			}
		};
	}

	/**
	 * The method returns a runnable task to get values for a list of oids
	 * 
	 * @param snmpConnection
	 * @param ret
	 * @param oids
	 * @param requestId 
	 * @param snmpErrorSet
	 * @param subId
	 * @return a task that performs get
	 */	
	protected Callable<SnmpMgrRespListener> getAsynchTaskForOids(
			final SnmpConnection snmpConnection, final List<VarBind> ret,
			final List<String> oids, int pduType, final String requestId, final int subId) {
		return new Callable<SnmpMgrRespListener>() {
			SnmpMgrRespListener listener = null;
			@Override
			public SnmpMgrRespListener call() {
				try {					
					long startTime = System.currentTimeMillis();
					String msg = requestId + ":" + subId + ":" + startTime + ":started:" + Thread.currentThread().getName()
							+ " : for oids " + oids;
					LoggerUtil.setLog(getClass(), LogConstants.DEBUG, msg);
					listener = SnmpCommunicationHandler.getAsynchOIDValues(
							snmpConnection, oids, PDU.GET, subId);

					long endTime = System.currentTimeMillis();
					long elapsedTime = endTime - startTime;
					String msg1 = requestId + ":" + subId + ":" + endTime + ":" + Thread.currentThread().getName() + " : for oids "
							+ oids + " took " + elapsedTime + " milliseconds";
					LoggerUtil.setLog(getClass(), LogConstants.DEBUG, msg1);
				} catch (SnmpManagerException se) {
					// other tasks may run successfully hence ignore this
					String msg = Thread.currentThread().getName()
							+ "Exception occured while fetching " + oids;
					LoggerUtil.setLog(getClass(), LogConstants.WARN, msg);
				}
				return listener;
			}
		};
	}
	
	/**
	 * This method logs the SNMP errors and also takes first of the errors back
	 * to the calling app
	 * 
	 * @param snmpErrorSet
	 * @param snmpError
	 */
	protected void handleSnmpErrors(final Set<SnmpError> snmpErrorSet,
			SnmpError snmpError) {
		for (SnmpError currSnmpErr : snmpErrorSet) {
			// logging in case there are errors
			if (currSnmpErr.getErrorStatus() != 0) {
				LoggerUtil.setLog(getClass(), LogConstants.ERROR,
						"Snmp Error occured");
				LoggerUtil.setLog(getClass(), LogConstants.ERROR,
						"Error Status: " + currSnmpErr.getErrorStatus());
				LoggerUtil.setLog(getClass(), LogConstants.ERROR,
						"Error Index: " + currSnmpErr.getErrorIndex());
				LoggerUtil.setLog(getClass(), LogConstants.ERROR,
						"Error Message: " + currSnmpErr.getErrorMessage());
			}
		}
		for (SnmpError currSnmpErr : snmpErrorSet) {
			// Adding the first error in the list. Ignoring others
			// as we can send only one error back to calling app
			if (currSnmpErr.getErrorStatus() != 0) {
				snmpError = currSnmpErr;
				break;
			}
		}
	}
	

	/**
	 * Divides a given list of oids into groups based on the vale of factor. For
	 * a list of 13 oids and factor 2 it will return 6 groups with 2 oids each
	 * and 1 groip with 1 oid. For a list of 13 oids and factor 7 it will return
	 * 2 groups, one with 7 oids and another with 6 oids.
	 * 
	 * @param oids
	 * @param factor
	 * @return list of sublists
	 */
	public List<List<String>> divide(List<String> oids, int factor) {
		List<List<String>> oidGroups = new ArrayList<List<String>>();
		if (factor == 0) {
			oidGroups.add(oids);
			return oidGroups;
		}
		int numGroups = Math.abs(oids.size() / factor);
		int remainder = oids.size() % factor;
		int i = 0;
		for (i = 0; i < numGroups; i++) {
			List<String> oidSubList = new ArrayList<String>();
			for (int j = i * factor; j < ((i * factor) + factor); j++) {
				oidSubList.add(oids.get(j));
			}
			oidGroups.add(oidSubList);
		}
		if (remainder != 0) {
			List<String> oidSubList = new ArrayList<String>();
			for (int k = i * factor; k < oids.size(); k++) {
				oidSubList.add(oids.get(k));
			}
			oidGroups.add(oidSubList);
		}
		logGroupedOids(oidGroups);
		return oidGroups;
	}

	/**
	 * this method logs the oid groups
	 * 
	 * @param oidGroups
	 */
	public void logGroupedOids(List<List<String>> oidGroups) {
		LoggerUtil.setLog(getClass(), LogConstants.DEBUG,
				"Listing OID Groups..");
		for (List<String> oids : oidGroups) {
			LoggerUtil.setLog(getClass(), LogConstants.DEBUG, oids.size() + " : " + oids.toString());
		}
	}
	
}
