/************************************************************************************************
 *  _________          _     ____          _           __        __    _ _      _   _   _ ___
 * |__  / ___|__ _ ___| |__ / ___|_      _(_)_ __   __ \ \      / /_ _| | | ___| |_| | | |_ _|
 *   / / |   / _` / __| '_ \\___ \ \ /\ / / | '_ \ / _` \ \ /\ / / _` | | |/ _ \ __| | | || |
 *  / /| |__| (_| \__ \ | | |___) \ V  V /| | | | | (_| |\ V  V / (_| | | |  __/ |_| |_| || |
 * /____\____\__,_|___/_| |_|____/ \_/\_/ |_|_| |_|\__, | \_/\_/ \__,_|_|_|\___|\__|\___/|___|
 *                                                 |___/
 *
 * Copyright (c) 2016 Ivan Vaklinov <ivan@vaklinov.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 **********************************************************************************/
package com.vaklinov.zcashui;


import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.eclipsesource.json.ParseException;
import com.eclipsesource.json.WriterConfig;


/**
 * Calls zcash-cli
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class ZCashClientCaller
{
	public static class WalletBalance
	{
		public double transparentBalance;
		public double privateBalance;
		public double totalBalance;

		public double transparentUnconfirmedBalance;
		public double privateUnconfirmedBalance;
		public double totalUnconfirmedBalance;
	}


	public static class NetworkAndBlockchainInfo
	{
		public int numConnections;
		public Date lastBlockDate;
	}


	public static class WalletCallException
		extends Exception
	{
		public WalletCallException(String message)
		{
			super(message);
		}

		public WalletCallException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}


	// ZCash client program
	private File zcashcli;


	public ZCashClientCaller(String installDir)
		throws IOException
	{
		// Detect daemon and client tools installation
		File dir = new File(installDir);
	    zcashcli = new File(dir, "komodo-cli");

		if (!zcashcli.exists())
		{
			zcashcli = OSUtil.findZCashCommand("komodo-cli");
		}

		if ((zcashcli == null) || (!zcashcli.exists()))
		{
			throw new IOException(
				"The Komodo installation directory " + installDir + " needs to contain " +
				"the command line utilities komodod and komodo-cli. komodo-cli is missing!");
		}
	}


	public synchronized WalletBalance getWalletInfo()
		throws WalletCallException, IOException, InterruptedException
	{
		WalletBalance balance = new WalletBalance();

		JsonObject objResponse = this.executeCommandAndGetJsonObject("z_gettotalbalance", null);

    	balance.transparentBalance = Double.valueOf(objResponse.getString("transparent", "-1"));
    	balance.privateBalance     = Double.valueOf(objResponse.getString("private", "-1"));
    	balance.totalBalance       = Double.valueOf(objResponse.getString("total", "-1"));

        objResponse = this.executeCommandAndGetJsonObject("z_gettotalbalance", "0");

    	balance.transparentUnconfirmedBalance = Double.valueOf(objResponse.getString("transparent", "-1"));
    	balance.privateUnconfirmedBalance     = Double.valueOf(objResponse.getString("private", "-1"));
    	balance.totalUnconfirmedBalance       = Double.valueOf(objResponse.getString("total", "-1"));

		return balance;
	}


	public synchronized String[][] getWalletPublicTransactions()
		throws WalletCallException, IOException, InterruptedException
	{
	    JsonArray jsonTransactions = executeCommandAndGetJsonArray("listtransactions", "", "100");
	    String strTransactions[][] = new String[jsonTransactions.size()][];
	    for (int i = 0; i < jsonTransactions.size(); i++)
	    {
	    	strTransactions[i] = new String[7];
	    	JsonObject trans = jsonTransactions.get(i).asObject();

	    	// Needs to be the same as in getWalletZReceivedTransactions()
	    	// TODO: some day refactor to use object containers
	    	strTransactions[i][0] = "\u2606T (Public)";
	    	strTransactions[i][1] = trans.getString("category", "ERROR!");
	    	strTransactions[i][2] = trans.get("confirmations").toString();
	    	strTransactions[i][3] = trans.get("amount").toString();
	    	strTransactions[i][4] = trans.get("time").toString();
	    	strTransactions[i][5] = trans.getString("address", "\u26D4 (Z Address not listed by wallet!)");
	    	strTransactions[i][6] = trans.get("txid").toString();

	    }

	    return strTransactions;
	}


	public synchronized String[] getWalletZAddresses()
		throws WalletCallException, IOException, InterruptedException
	{
		JsonArray jsonAddresses = executeCommandAndGetJsonArray("z_listaddresses", null);
		String strAddresses[] = new String[jsonAddresses.size()];
		for (int i = 0; i < jsonAddresses.size(); i++)
		{
		    strAddresses[i] = jsonAddresses.get(i).asString();
		}

	    return strAddresses;
	}


	public synchronized String[][] getWalletZReceivedTransactions()
		throws WalletCallException, IOException, InterruptedException
	{
		String[] zAddresses = this.getWalletZAddresses();

		List<String[]> zReceivedTransactions = new ArrayList<String[]>();

		for (String zAddress : zAddresses)
		{
		    JsonArray jsonTransactions = executeCommandAndGetJsonArray("z_listreceivedbyaddress", zAddress, "0");
		    for (int i = 0; i < jsonTransactions.size(); i++)
		    {
		    	String[] currentTransaction = new String[7];
		    	JsonObject trans = jsonTransactions.get(i).asObject();

		    	String txID = trans.getString("txid", "ERROR!");
		    	// Needs to be the same as in getWalletPublicTransactions()
		    	// TODO: some day refactor to use object containers
		    	currentTransaction[0] = "\u2605Z (Private)";
		    	currentTransaction[1] = "receive";
		    	currentTransaction[2] = this.getWalletTransactionConfirmations(txID);
		    	currentTransaction[3] = trans.get("amount").toString();
		    	currentTransaction[4] = this.getWalletTransactionTime(txID); // TODO: minimize sub-calls
		    	currentTransaction[5] = zAddress;
		    	currentTransaction[6] = trans.get("txid").toString();

		    	zReceivedTransactions.add(currentTransaction);
		    }
		}

		return zReceivedTransactions.toArray(new String[0][]);
	}


	// ./src/zcash-cli listunspent only returns T addresses it seems
	public synchronized String[] getWalletPublicAddressesWithUnspentOutputs()
		throws WalletCallException, IOException, InterruptedException
	{
		JsonArray jsonUnspentOutputs = executeCommandAndGetJsonArray("listunspent", "0");

		Set<String> addresses = new HashSet<>();
	    for (int i = 0; i < jsonUnspentOutputs.size(); i++)
	    {
	    	JsonObject outp = jsonUnspentOutputs.get(i).asObject();
	    	addresses.add(outp.getString("address", "ERROR!"));
	    }

	    return addresses.toArray(new String[0]);
     }


	// ./zcash-cli listreceivedbyaddress 0 true
	public synchronized String[] getWalletAllPublicAddresses()
		throws WalletCallException, IOException, InterruptedException
	{
		JsonArray jsonReceivedOutputs = executeCommandAndGetJsonArray("listreceivedbyaddress", "0", "true");

		Set<String> addresses = new HashSet<>();
		for (int i = 0; i < jsonReceivedOutputs.size(); i++)
		{
		   	JsonObject outp = jsonReceivedOutputs.get(i).asObject();
		   	addresses.add(outp.getString("address", "ERROR!"));
		}

		return addresses.toArray(new String[0]);
    }

	
	public synchronized Map<String, String> getRawTransactionDetails(String txID)
		throws WalletCallException, IOException, InterruptedException
	{
		JsonObject jsonTransaction = this.executeCommandAndGetJsonObject("gettransaction", txID);

		Map<String, String> map = new HashMap<String, String>();

		for (String name : jsonTransaction.names())
		{
			this.decomposeJSONValue(name, jsonTransaction.get(name), map);
		}
				
		return map;
	}
	
	
	public synchronized String getRawTransaction(String txID)
		throws WalletCallException, IOException, InterruptedException
	{
		JsonObject jsonTransaction = this.executeCommandAndGetJsonObject("gettransaction", txID);

		return jsonTransaction.toString(WriterConfig.PRETTY_PRINT);
	}


	// return UNIX time as tring
	public synchronized String getWalletTransactionTime(String txID)
		throws WalletCallException, IOException, InterruptedException
	{
		JsonObject jsonTransaction = this.executeCommandAndGetJsonObject("gettransaction", txID);

		return String.valueOf(jsonTransaction.getLong("time", -1));
	}
	
	
	public synchronized String getWalletTransactionConfirmations(String txID)
		throws WalletCallException, IOException, InterruptedException
	{
		JsonObject jsonTransaction = this.executeCommandAndGetJsonObject("gettransaction", txID);

		return jsonTransaction.get("confirmations").toString();
	}
	

	// Returns confirmed balance only!
	public synchronized String getBalanceForAddress(String address)
		throws WalletCallException, IOException, InterruptedException
	{
	    JsonValue response = this.executeCommandAndGetJsonValue("z_getbalance", address);

		return String.valueOf(response.toString());
	}


	public synchronized String getUnconfirmedBalanceForAddress(String address)
		throws WalletCallException, IOException, InterruptedException
	{
	    JsonValue response = this.executeCommandAndGetJsonValue("z_getbalance", address, "0");

		return String.valueOf(response.toString());
	}


	public synchronized String createNewAddress(boolean isZAddress)
		throws WalletCallException, IOException, InterruptedException
	{
	    String strResponse = this.executeCommandAndGetSingleStringResponse((isZAddress ? "z_" : "") + "getnewaddress");

		return strResponse.trim();
	}


	// Returns OPID
	public synchronized String sendCash(String from, String to, String amount, String memo)
		throws WalletCallException, IOException, InterruptedException
	{
		StringBuilder hexMemo = new StringBuilder();
		for (byte c : memo.getBytes("UTF-8"))
		{
			String hexChar = Integer.toHexString((int)c);
			if (hexChar.length() < 2)
			{
				hexChar = "0" + hexChar;
			}
			hexMemo.append(hexChar);
		}

		JsonObject toArgument = new JsonObject();
		toArgument.set("address", to);
		if (hexMemo.length() >= 2)
		{
			toArgument.set("memo", hexMemo.toString());
		}

		// The JSON Builder has a problem with double values that have no fractional part
		// it serializes them as integers that ZCash does not accept. So we do a replacement
		// TODO: find a better/cleaner way to format the amount
		toArgument.set("amount", "\uFFFF\uFFFF\uFFFF\uFFFF\uFFFF");

		JsonArray toMany = new JsonArray();
		toMany.add(toArgument);
		
		String amountPattern = "\"amount\":\"\uFFFF\uFFFF\uFFFF\uFFFF\uFFFF\"";
		// Make sure our replacement hack never leads to a mess up
		String toManyBeforeReplace = toMany.toString();
		int firstIndex = toManyBeforeReplace.indexOf(amountPattern);
		int lastIndex = toManyBeforeReplace.lastIndexOf(amountPattern);
		if ((firstIndex == -1) || (firstIndex != lastIndex))
		{
			throw new WalletCallException("Error in forming z_sendmany command: " + toManyBeforeReplace);
		}

		DecimalFormatSymbols decSymbols = new DecimalFormatSymbols(Locale.ROOT);
		String[] sendCashParameters = new String[]
	    {
		    this.zcashcli.getCanonicalPath(), "z_sendmany", from,
		    // This replacement is a hack to make sure the JSON object amount has double format 0.00 etc.
		    // TODO: find a better way to format the amount
		    toMany.toString().replace(
		    	amountPattern,
		    	"\"amount\":" + new DecimalFormat("########0.00######", decSymbols).format(Double.valueOf(amount)))
		};

	    CommandExecutor caller = new CommandExecutor(sendCashParameters);
	    String strResponse = caller.execute();

		if (strResponse.trim().startsWith("error:"))
		{
		  	throw new WalletCallException("Error response from wallet: " + strResponse);
		}

		System.out.println("Sending cash with the following command: " +
                sendCashParameters[0] + " " + sendCashParameters[1] + " " +
                sendCashParameters[2] + " " + sendCashParameters[3] + "." +
                " Got result: [" + strResponse + "]");

		return strResponse.trim();
	}


	public boolean isSendingOperationComplete(String opID)
	    throws WalletCallException, IOException, InterruptedException
	{
		JsonArray response = this.executeCommandAndGetJsonArray("z_getoperationstatus", "[\"" + opID + "\"]");
		JsonObject jsonStatus = response.get(0).asObject();

		String status = jsonStatus.getString("status", "ERROR");

		System.out.println("Operation " + opID + " status is " + response + ".");

		if (status.equalsIgnoreCase("success") ||
			status.equalsIgnoreCase("error") ||
			status.equalsIgnoreCase("failed"))
		{
			return true;
		} else if (status.equalsIgnoreCase("executing") || status.equalsIgnoreCase("queued"))
		{
			return false;
		} else
		{
			throw new WalletCallException("Unexpected status response from wallet: " + response.toString());
		}
	}


	public boolean isCompletedOperationSuccessful(String opID)
	    throws WalletCallException, IOException, InterruptedException
	{
		JsonArray response = this.executeCommandAndGetJsonArray("z_getoperationstatus", "[\"" + opID + "\"]");
		JsonObject jsonStatus = response.get(0).asObject();

		String status = jsonStatus.getString("status", "ERROR");

		System.out.println("Operation " + opID + " status is " + response + ".");

		if (status.equalsIgnoreCase("success"))
		{
			return true;
		} else if (status.equalsIgnoreCase("error") || status.equalsIgnoreCase("failed"))
		{
			return false;
		} else
		{
			throw new WalletCallException("Unexpected final operation status response from wallet: " + response.toString());
		}
	}


	// May only be called for already failed operations
	public String getOperationFinalErrorMessage(String opID)
	    throws WalletCallException, IOException, InterruptedException
	{
		JsonArray response = this.executeCommandAndGetJsonArray("z_getoperationstatus", "[\"" + opID + "\"]");
		JsonObject jsonStatus = response.get(0).asObject();

		JsonObject jsonError = jsonStatus.get("error").asObject();
		return jsonError.getString("message", "ERROR!");
	}


	public NetworkAndBlockchainInfo getNetworkAndBlockchainInfo()
		throws WalletCallException, IOException, InterruptedException
	{
		NetworkAndBlockchainInfo info = new NetworkAndBlockchainInfo();

		String strNumCons = this.executeCommandAndGetSingleStringResponse("getconnectioncount");
		info.numConnections = Integer.valueOf(strNumCons.trim());

		String strBlockCount = this.executeCommandAndGetSingleStringResponse("getblockcount");
		String lastBlockHash = this.executeCommandAndGetSingleStringResponse("getblockhash", strBlockCount.trim());
		JsonObject lastBlock = this.executeCommandAndGetJsonObject("getblock", lastBlockHash.trim());
		info.lastBlockDate = new Date(Long.valueOf(lastBlock.getLong("time", -1) * 1000L));

		return info;
	}


	public void lockWallet()
		throws WalletCallException, IOException, InterruptedException
	{
		String response = this.executeCommandAndGetSingleStringResponse("walletlock");

		// Response is expected to be empty
		if (response.trim().length() > 0)
		{
			throw new WalletCallException("Unexpected response from wallet: " + response);
		}
	}


	// Unlocks the wallet for 5 minutes - meant to be followed shortly by lock!
	// TODO: tests with a password containing spaces
	public void unlockWallet(String password)
		throws WalletCallException, IOException, InterruptedException
	{
		String response = this.executeCommandAndGetSingleStringResponse(
			"walletpassphrase", password, "300");

		// Response is expected to be empty
		if (response.trim().length() > 0)
		{
			throw new WalletCallException("Unexpected response from wallet: " + response);
		}
	}


    // Wallet locks check - an unencrypted wallet will give an error
	// zcash-cli walletlock
	// error: {"code":-15,"message":"Error: running with an unencrypted wallet, but walletlock was called."}
	public boolean isWalletEncrypted()
   		throws WalletCallException, IOException, InterruptedException
    {
		String[] params = new String[] { this.zcashcli.getCanonicalPath(), "walletlock" };
		CommandExecutor caller = new CommandExecutor(params);
    	String strResult = caller.execute();

    	 if (strResult.trim().length() <= 0)
    	 {
    		 // If it could be locked with no result - obviously encrypted
    		 return true;
    	 } else if (strResult.trim().startsWith("error:"))
    	 {
    		 // Expecting an error of an unencrypted wallet
    		 String jsonPart = strResult.substring(strResult.indexOf("{"));
   			 JsonValue response = null;
   			 try
   			 {
   			   	response = Json.parse(jsonPart);
   		 	 } catch (ParseException pe)
   			 {
   			   	 throw new WalletCallException(jsonPart + "\n" + pe.getMessage() + "\n", pe);
   			 }

   			 JsonObject respObject = response.asObject();
   			 if ((respObject.getDouble("code", -1) == -15) &&
   				 (respObject.getString("message", "ERR").indexOf("unencrypted wallet") != -1))
   			 {
   				 // Obviously unencrupted
   				 return false;
   			 } else
   			 {
   	    		 throw new WalletCallException("Unexpected response from wallet: " + strResult);
   			 }
    	 } else
    	 {
    		 throw new WalletCallException("Unexpected response from wallet: " + strResult);
    	 }
    }


	/**
	 * Encrypts the wallet. Typical success/error use cases are:
	 *
	 * ./zcash-cli encryptwallet "1234"
	 * wallet encrypted; Bitcoin server stopping, restart to run with encrypted wallet.
	 * The keypool has been flushed, you need to make a new backup.
	 *
	 * ./zcash-cli encryptwallet "1234"
	 * error: {"code":-15,"message":"Error: running with an encrypted wallet, but encryptwallet was called."}
	 *
	 * @param password
	 */
	public void encryptWallet(String password)
		throws WalletCallException, IOException, InterruptedException
	{
		String response = this.executeCommandAndGetSingleStringResponse("encryptwallet", password);
		System.out.println("Result of wallet encryption is: \n" + response);
		// If no exception - obviously successful
	}
	
	
	public void backupWallet(String fileName)
		throws WalletCallException, IOException, InterruptedException
	{
		System.out.println("Backup up wallet to location: " + fileName);
		String response = this.executeCommandAndGetSingleStringResponse("backupwallet", fileName);
		// If no exception - obviously successful		
	}
	
	
	public void exportWallet(String fileName)
		throws WalletCallException, IOException, InterruptedException
	{
		System.out.println("Export wallet keys to location: " + fileName);
		String response = this.executeCommandAndGetSingleStringResponse("z_exportwallet", fileName);
		// If no exception - obviously successful		
	}


	private JsonObject executeCommandAndGetJsonObject(String command1, String command2)
		throws WalletCallException, IOException, InterruptedException
	{
		JsonValue response = this.executeCommandAndGetJsonValue(command1, command2);

		if (response.isObject())
		{
			return response.asObject();
		} else
		{
			throw new WalletCallException("Unexpected non-object response from wallet: " + response.toString());
		}

	}


	private JsonArray executeCommandAndGetJsonArray(String command1, String command2)
		throws WalletCallException, IOException, InterruptedException
	{
		return this.executeCommandAndGetJsonArray(command1, command2, null);
	}


	private JsonArray executeCommandAndGetJsonArray(String command1, String command2, String command3)
		throws WalletCallException, IOException, InterruptedException
	{
		JsonValue response = this.executeCommandAndGetJsonValue(command1, command2, command3);

		if (response.isArray())
		{
			return response.asArray();
		} else
		{
			throw new WalletCallException("Unexpected non-array response from wallet: " + response.toString());
		}
	}


	private JsonValue executeCommandAndGetJsonValue(String command1, String command2)
			throws WalletCallException, IOException, InterruptedException
	{
		return this.executeCommandAndGetJsonValue(command1, command2, null);
	}


	private JsonValue executeCommandAndGetJsonValue(String command1, String command2, String command3)
		throws WalletCallException, IOException, InterruptedException
	{
		String strResponse = this.executeCommandAndGetSingleStringResponse(command1, command2, command3);

		JsonValue response = null;
		try
		{
		  	response = Json.parse(strResponse);
		} catch (ParseException pe)
		{
		  	throw new WalletCallException(strResponse + "\n" + pe.getMessage() + "\n", pe);
		}

		return response;
	}


	private String executeCommandAndGetSingleStringResponse(String command1)
		throws WalletCallException, IOException, InterruptedException
	{
		return this.executeCommandAndGetSingleStringResponse(command1, null);
	}


	private String executeCommandAndGetSingleStringResponse(String command1, String command2)
		throws WalletCallException, IOException, InterruptedException
	{
		return this.executeCommandAndGetSingleStringResponse(command1, command2, null);
	}


	private String executeCommandAndGetSingleStringResponse(String command1, String command2, String command3)
		throws WalletCallException, IOException, InterruptedException
	{
		String[] params;
		if (command3 != null)
		{
			params = new String[] { this.zcashcli.getCanonicalPath(), command1, command2, command3 };
		} else if (command2 != null)
		{
			params = new String[] { this.zcashcli.getCanonicalPath(), command1, command2 };
		} else
		{
			params = new String[] { this.zcashcli.getCanonicalPath(), command1 };
		}

	    CommandExecutor caller = new CommandExecutor(params);

		String strResponse = caller.execute();
		if (strResponse.trim().startsWith("error:"))
		{
		  	throw new WalletCallException("Error response from wallet: " + strResponse);
		}

		return strResponse;
	}
	
	
	private void decomposeJSONValue(String name, JsonValue val, Map<String, String> map)
	{
		if (val.isObject())
		{
			JsonObject obj = val.asObject();
			for (String memberName : obj.names())
			{
				this.decomposeJSONValue(name + "." + memberName, obj.get(memberName), map);
			}
		} else if (val.isArray())
		{
			JsonArray arr = val.asArray();
			for (int i = 0; i < arr.size(); i++)
			{
				this.decomposeJSONValue(name + "[" + i + "]", arr.get(i), map);
			}
		} else
		{
			map.put(name, val.toString());
		}
	}

}
