package org.vl.xmlrpc;

import java.util.Vector;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

public class CachedXMLRPCClient extends XMLRPCClient {
	
	protected String cacheDirectory;
	
	/**
	 * Set cache directory
	 * 
	 * @param absolute path to where store cache files
	 */
	public void setCacheFilesDir(String cache_directory) {
		cacheDirectory = cache_directory;
	}

	/**
	 * Set cache maximum size
	 * 
	 * @param maximum size in bytes
	 */
	public void setMaxCacheSize(int bytes) {
		
	}

	/**
	 * Get a cached result from a cacheid
	 * 
	 * @param cache id
	 */
	private Object getCachedResult(String cacheid) {
		return null;
	}

	/**
	 * Store a cached result, using a cacheid
	 * 
	 * @param cache id
	 * @param object to store
	 */
	private void putInCache(String cacheid, Object object) {
		
	}
	
	/**
	 * convenience method to do the actual call
	 * 
	 * @param name of method to call
	 * @param object to store
	 * @param parameters to pass to method (may be null if method has no parameters)
	 * @param use cache for getting the result, or not
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
	public Object callEx(String method, Object[] params, boolean cached) throws XMLRPCException {
		String cacheId = method + String.valueOf(params.hashCode());
		Object validCache = getCachedResult(cacheId);
		
		if (validCache != null)
			return validCache;
		
		try {
			Object result = super.callEx(method, params);
			
			try {
				putInCache(cacheId, result);
			}
			catch (Exception e) {
				// Fail to put result in cache
			}
			return result;
		}
		catch (XMLRPCException e) {
			throw e;
		}
	}

	/*************************************************************************************/
	
	public CachedXMLRPCClient(String url) {
		super(url);
	}

	/**
	 * Convenience method call with no parameters
	 * 
	 * @param method name of method to call
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
	public Object call(String method, boolean cached) throws XMLRPCException {
		return callEx(method, null, cached);
	}
	
	/**
	 * Convenience method call with a vectorized parameter
     * (Code contributed by jahbromo from issue #14)
	 * @param method name of method to call
	 * @param paramsv vector of method's parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
	
	public Object call(String method, Vector paramsv, boolean cached) throws XMLRPCException {
		Object[] params = new Object [paramsv.size()];
		for (int i=0; i<paramsv.size(); i++) {
			params[i]=paramsv.elementAt(i);
		}
		return callEx(method, params, cached);
	}
	
	/**
	 * Convenience method call with one parameter
	 * 
	 * @param method name of method to call
	 * @param p0 method's parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
	public Object call(String method, Object p0, boolean cached) throws XMLRPCException {
		Object[] params = {
			p0,
		};
		return callEx(method, params, cached);
	}
	
	/**
	 * Convenience method call with two parameters
	 * 
	 * @param method name of method to call
	 * @param p0 method's 1st parameter
	 * @param p1 method's 2nd parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
	public Object call(String method, Object p0, Object p1, boolean cached) throws XMLRPCException {
		Object[] params = {
			p0, p1,
		};
		return callEx(method, params, cached);
	}
	
	/**
	 * Convenience method call with three parameters
	 * 
	 * @param method name of method to call
	 * @param p0 method's 1st parameter
	 * @param p1 method's 2nd parameter
	 * @param p2 method's 3rd parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
	public Object call(String method, Object p0, Object p1, Object p2, boolean cached) throws XMLRPCException {
		Object[] params = {
			p0, p1, p2,
		};
		return callEx(method, params, cached);
	}

	/**
	 * Convenience method call with four parameters
	 * 
	 * @param method name of method to call
	 * @param p0 method's 1st parameter
	 * @param p1 method's 2nd parameter
	 * @param p2 method's 3rd parameter
	 * @param p3 method's 4th parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
	public Object call(String method, Object p0, Object p1, Object p2, Object p3, boolean cached) throws XMLRPCException {
		Object[] params = {
			p0, p1, p2, p3,
		};
		return callEx(method, params, cached);
	}

	/**
	 * Convenience method call with five parameters
	 * 
	 * @param method name of method to call
	 * @param p0 method's 1st parameter
	 * @param p1 method's 2nd parameter
	 * @param p2 method's 3rd parameter
	 * @param p3 method's 4th parameter
	 * @param p4 method's 5th parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
	public Object call(String method, Object p0, Object p1, Object p2, Object p3, Object p4, boolean cached) throws XMLRPCException {
		Object[] params = {
			p0, p1, p2, p3, p4,
		};
		return callEx(method, params, cached);
	}

	/**
	 * Convenience method call with six parameters
	 * 
	 * @param method name of method to call
	 * @param p0 method's 1st parameter
	 * @param p1 method's 2nd parameter
	 * @param p2 method's 3rd parameter
	 * @param p3 method's 4th parameter
	 * @param p4 method's 5th parameter
	 * @param p5 method's 6th parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
	public Object call(String method, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, boolean cached) throws XMLRPCException {
		Object[] params = {
			p0, p1, p2, p3, p4, p5,
		};
		return callEx(method, params, cached);
	}

	/**
	 * Convenience method call with seven parameters
	 * 
	 * @param method name of method to call
	 * @param p0 method's 1st parameter
	 * @param p1 method's 2nd parameter
	 * @param p2 method's 3rd parameter
	 * @param p3 method's 4th parameter
	 * @param p4 method's 5th parameter
	 * @param p5 method's 6th parameter
	 * @param p6 method's 7th parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
	public Object call(String method, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, boolean cached) throws XMLRPCException {
		Object[] params = {
			p0, p1, p2, p3, p4, p5, p6,
		};
		return callEx(method, params, cached);
	}

	/**
	 * Convenience method call with eight parameters
	 * 
	 * @param method name of method to call
	 * @param p0 method's 1st parameter
	 * @param p1 method's 2nd parameter
	 * @param p2 method's 3rd parameter
	 * @param p3 method's 4th parameter
	 * @param p4 method's 5th parameter
	 * @param p5 method's 6th parameter
	 * @param p6 method's 7th parameter
	 * @param p7 method's 8th parameter
	 * @return deserialized method return value
	 * @throws XMLRPCException
	 */
	public Object call(String method, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, boolean cached) throws XMLRPCException {
		Object[] params = {
			p0, p1, p2, p3, p4, p5, p6, p7,
		};
		return callEx(method, params, cached);
	}
	
}
