/*
 *      Copyright (C) 2005-2015 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.android.jsonrpc.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xbmc.android.jsonrpc.service.AudioSyncService;
import org.xbmc.android.util.google.HttpHelper;

import android.util.Log;

/**
 * Parent class for all "standalone" clients. Contains network logic 
 * inclusively error handling.
 * 
 * @author freezy <freezy@xbmc.org>
 */
public abstract class AbstractClient {
	
	private final static String TAG = AbstractClient.class.getSimpleName();
	
	/**
	 * Synchronously posts the request object to XBMC's JSONRPC API and returns
	 * the unserialized response as {@link JSONObject}.
	 * 
	 * @param entity Request object generated by API class
	 * @param errorHandler Error handler in case something goes wrong
	 * @return Response as JSONObject
	 */
	protected JSONObject execute(JSONObject entity, ErrorHandler errorHandler) {
		
		final HttpClient httpClient = HttpHelper.getHttpClient();
		final HttpPost request = new HttpPost(AudioSyncService.URL);
		InputStream input = null;
		
		try {
			Log.i(TAG, "POSTING: " + entity.toString());
			
			request.setEntity(new StringEntity(entity.toString(), "UTF-8"));
			request.addHeader("Content-Type", "application/json");
			
			final HttpResponse resp = httpClient.execute(request);
			final int status = resp.getStatusLine().getStatusCode();
			if (status != HttpStatus.SC_OK && errorHandler != null) {
				errorHandler.handleError(ErrorHandler.UNEXPECTED_SERVER_RESPONSE, "Unexpected server response " + resp.getStatusLine() + " for " + request.getRequestLine());
			} else {
				input = resp.getEntity().getContent();
				final BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"), 8192);
				final StringBuilder sb = new StringBuilder();
				for (String line = null; (line = reader.readLine()) != null;) {
					sb.append(line).append("\n");
				}
				Log.d(TAG, "RESPONSE: " + sb.toString());
				
				final JSONTokener tokener = new JSONTokener(sb.toString());
				final JSONObject response = ((JSONObject)tokener.nextValue());
				if (response.has("error") && errorHandler != null) {
					errorHandler.handleError(ErrorHandler.API_ERROR, response.getJSONObject("error").getString("message"));
				} else if (response.has("result")) {
					return response.getJSONObject("result");
				} else {
					if (errorHandler != null) {
						errorHandler.handleError(ErrorHandler.NO_RESULT_FOUND, "No 'result' element in JSON response.");
					}
					Log.e(TAG, "RESPONSE: " + sb.toString());
				}
			}
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage(), e);
			if (errorHandler != null) {
				errorHandler.handleError(ErrorHandler.UNSUPPORTED_ENCODING, e.getMessage());
			}
		} catch (IllegalStateException e) {
			Log.e(TAG, e.getMessage(), e);
			if (errorHandler != null) {
				errorHandler.handleError(ErrorHandler.ILLEGAL_STATE, e.getMessage());
			}
		} catch (HttpHostConnectException e) {
			Log.e(TAG, e.getMessage(), e);
			if (errorHandler != null) {
				errorHandler.handleError(ErrorHandler.HOST_CONNECTION, e.getMessage());
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			if (errorHandler != null) {
				errorHandler.handleError(ErrorHandler.IO_EXCEPTION, e.getMessage());
			}
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			if (errorHandler != null) {
				errorHandler.handleError(ErrorHandler.JSON_EXCEPTION, e.getMessage());
			}
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) { }
			}
		}
		return null;
	}
	
	/**
	 * Defines error codes and an action that is executed on error. Is generally
	 * executed on the UI thread.
	 */
	public interface ErrorHandler {
		public final static int UNEXPECTED_SERVER_RESPONSE = 1000;
		public final static int UNSUPPORTED_ENCODING = 1001;
		public final static int ILLEGAL_STATE = 1002;
		public final static int IO_EXCEPTION = 1003;
		public final static int JSON_EXCEPTION = 1004;
		public final static int API_ERROR = 1005;
		public final static int NO_RESULT_FOUND = 1006;
		public final static int HOST_CONNECTION = 1007;
		
		/**
		 * Implement your error logic here.
		 * @param code Error code as defined above
		 * @param message Error message in english. For translations, refer to the error code.
		 */
		void handleError(int code, String message);
	}
}