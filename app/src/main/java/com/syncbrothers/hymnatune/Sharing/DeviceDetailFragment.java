package com.syncbrothers.hymnatune.Sharing;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.syncbrothers.hymnatune.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {
	//static InterstitialAd mInterstitialAd;
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;

    private static ProgressDialog mProgressDialog;

    public static String WiFiServerIp = "";
	public static String WiFiClientIp = "";
	static Boolean ClientCheck = false;
	public static String GroupOwnerAddress = "";
	static long ActualFilelength = 0;
	static int Percentage = 0;
	public static String FolderName = "P2P";


	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog= ProgressDialog.show(getActivity(),"Press <- to cancel","Connecting:"+device.deviceAddress,true,true);
                ((DeviceActionListener) getActivity()).connect(config);
            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_pic).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                    }
                });
		mContentView.findViewById(R.id.btn_audio).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("audio/*");
						startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
					}
				});
		mContentView.findViewById(R.id.btn_video).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_PICK);
						intent.setType("video/*");
						startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
					}
				});
		mContentView.findViewById(R.id.btn_pdf).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("application/*");
						startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
					}
				});
		mContentView.findViewById(R.id.btn_text).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("plain/text");
						startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
					}
				});
		mContentView.findViewById(R.id.btn_word).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("application/msword");
						startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
					}
				});
		mContentView.findViewById(R.id.btn_ppt).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("application/vnd.ms-powerpoint");
						startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
					}
				});
		mContentView.findViewById(R.id.btn_apk).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("*/*");
						startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
					}
				});

		mContentView.findViewById(R.id.btn_audio).setBackgroundResource(R.drawable.audio_img);
		mContentView.findViewById(R.id.btn_audio).getLayoutParams().width= 80;
		mContentView.findViewById(R.id.btn_audio).getLayoutParams().height= 80;
		mContentView.findViewById(R.id.btn_video).setBackgroundResource(R.drawable.video_img);
		mContentView.findViewById(R.id.btn_video).getLayoutParams().width= 80;
		mContentView.findViewById(R.id.btn_video).getLayoutParams().height= 80;
		mContentView.findViewById(R.id.btn_pic).setBackgroundResource(R.drawable.pic_img);
		mContentView.findViewById(R.id.btn_pic).getLayoutParams().width= 80;
		mContentView.findViewById(R.id.btn_pic).getLayoutParams().height= 80;
		mContentView.findViewById(R.id.btn_connect).setBackgroundResource(R.drawable.connect);
		mContentView.findViewById(R.id.btn_connect).getLayoutParams().width= 80;
		mContentView.findViewById(R.id.btn_connect).getLayoutParams().height= 80;
		mContentView.findViewById(R.id.btn_disconnect).setBackgroundResource(R.drawable.dis_con);
		mContentView.findViewById(R.id.btn_disconnect).getLayoutParams().width= 80;
		mContentView.findViewById(R.id.btn_disconnect).getLayoutParams().height= 80;
		mContentView.findViewById(R.id.btn_pdf).setBackgroundResource(R.drawable.pdf_pic);
		mContentView.findViewById(R.id.btn_pdf).getLayoutParams().width= 80;
		mContentView.findViewById(R.id.btn_pdf).getLayoutParams().height= 80;
		mContentView.findViewById(R.id.btn_text).setBackgroundResource(R.drawable.pic_txt);
		mContentView.findViewById(R.id.btn_text).getLayoutParams().width= 80;
		mContentView.findViewById(R.id.btn_text).getLayoutParams().height= 80;
		mContentView.findViewById(R.id.btn_word).setBackgroundResource(R.drawable.word_pic);
		mContentView.findViewById(R.id.btn_word).getLayoutParams().width= 80;
		mContentView.findViewById(R.id.btn_word).getLayoutParams().height= 80;
		mContentView.findViewById(R.id.btn_ppt).setBackgroundResource(R.drawable.ppt_pic);
		mContentView.findViewById(R.id.btn_ppt).getLayoutParams().width= 80;
		mContentView.findViewById(R.id.btn_ppt).getLayoutParams().height= 80;
		mContentView.findViewById(R.id.btn_apk).setBackgroundResource(R.drawable.apk_pic);
		mContentView.findViewById(R.id.btn_apk).getLayoutParams().width= 80;
		mContentView.findViewById(R.id.btn_apk).getLayoutParams().height= 80;

		return mContentView;
    }

	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(resultCode == getActivity().RESULT_OK){
    		 Uri uri = data.getData();
    		 String selectedfilePath = null;
    		 try {
    			 selectedfilePath = CommonMethods.getPath(uri, getActivity());
     			 Log.e("File Path:", selectedfilePath);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
    			String Extension = "";
    			if(selectedfilePath!=null){
    				File f = new File(selectedfilePath);
        			System.out.println("File Name:" + f.getName());
        			Long FileLength = f.length();
        			ActualFilelength = FileLength;
        			try {
        				Extension = f.getName();
        				Log.e("Extension", "" + Extension);
        			} catch (Exception e) {
        				// TODO: handle exception
        				e.printStackTrace();
        			}
    			}
    			else{
    				CommonMethods.e("", "path is null");
    				return;
    			}
    			
    			
    	        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
    	        statusText.setText("Sending: " + uri);
    	        Log.d(WiFiDirectActivity.TAG, "Intent----------- " + uri);
    	        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
    	        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
    	        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
    	        String Ip = SharedPreferencesHandler.getStringValues(getActivity(), "WiFiClientIp");
    			String OwnerIp = SharedPreferencesHandler.getStringValues(getActivity(), "GroupOwnerAddress");
    			if (OwnerIp != null && OwnerIp.length() > 0) {
    				CommonMethods.e("", "inside the check:");
    				String host=null;
    				int  sub_port =-1;
    				
    				String ServerBool = SharedPreferencesHandler.getStringValues(getActivity(), "ServerBoolean");
    				if (ServerBool!=null && !ServerBool.equals("") && ServerBool.equalsIgnoreCase("true")) {
    					
    					//-----------------------------
    					if (Ip != null && !Ip.equals("")) {
    						CommonMethods.e(
    								"in if condition",
    								"Sending data to " + Ip);
    						// Get Client Ip Address and send data
    						host=Ip;
    						sub_port=FileTransferService.PORT;
    							serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, Ip);
    					}
    				}
					else {
    					CommonMethods.e("in else condition", "Sending data to " + OwnerIp);
    					FileTransferService.PORT = 8888;
    					host=OwnerIp;
    					sub_port=FileTransferService.PORT;
    					serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS, OwnerIp);
    				}
    				serviceIntent.putExtra(FileTransferService.Extension, Extension);
    				serviceIntent.putExtra(FileTransferService.Filelength, ActualFilelength + "");
    				serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);
    				if(host !=null && sub_port!=-1)
    				{
    					CommonMethods.e("Going to intiate service", "service intent for initiating transfer");
    					showprogress("Sending...");
    					getActivity().startService(serviceIntent);
    				}
    				else {
    					CommonMethods.DisplayToast(getActivity(),"Host Address not found, Please Re-Connect");
    					DismissProgressDialog();
    				}
    				
    			}
				else {
    				DismissProgressDialog();CommonMethods.DisplayToast(getActivity(),"Host Address not found,Please Re-Connect");
    			}
    	}
    	else{
    		CommonMethods.DisplayToast(getActivity(), "Cancelled Request");
    	}
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);
        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)+((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                        : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        if(info.groupOwnerAddress.getHostAddress()!=null)
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
        else{
        	CommonMethods.DisplayToast(getActivity(), "Host Address not found");
        }

        try {
			String GroupOwner = info.groupOwnerAddress.getHostAddress();
			if(GroupOwner!=null && !GroupOwner.equals(""))
				SharedPreferencesHandler.setStringValues(getActivity(), "GroupOwnerAddress", GroupOwner);
			mContentView.findViewById(R.id.btn_pic).setVisibility(View.VISIBLE);
			mContentView.findViewById(R.id.btn_audio).setVisibility(View.VISIBLE);
			mContentView.findViewById(R.id.btn_video).setVisibility(View.VISIBLE);
			mContentView.findViewById(R.id.btn_pdf).setVisibility(View.VISIBLE);
			mContentView.findViewById(R.id.btn_text).setVisibility(View.VISIBLE);
			mContentView.findViewById(R.id.btn_ppt).setVisibility(View.VISIBLE);
			mContentView.findViewById(R.id.btn_word).setVisibility(View.VISIBLE);
			mContentView.findViewById(R.id.btn_apk).setVisibility(View.VISIBLE);
        if (info.groupFormed && info.isGroupOwner) {
        	SharedPreferencesHandler.setStringValues(getActivity(),"ServerBoolean", "true");
        	FileServerAsyncTask FileServerobj=new FileServerAsyncTask(getActivity(),FileTransferService.PORT);
			if (FileServerobj != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					FileServerobj.executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR,
							new String[] { null });
				}
				else
					FileServerobj.execute();
			}
        }
        else  {
        	if (!ClientCheck) {
				firstConnectionMessage firstObj = new firstConnectionMessage(GroupOwnerAddress);
				if (firstObj != null) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						firstObj.executeOnExecutor(
								AsyncTask.THREAD_POOL_EXECUTOR,
								new String[] { null });
					} else
						firstObj.execute();
				}
			}
        	
        	FileServerAsyncTask FileServerobj = new FileServerAsyncTask(getActivity(), FileTransferService.PORT);
			if (FileServerobj != null) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					FileServerobj.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,new String[]{null});
				}
				else
					FileServerobj.execute();
			}
        }
        }
        catch(Exception e){
        }
    }

    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());
    }

    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_pic).setVisibility(View.GONE);
		mContentView.findViewById(R.id.btn_audio).setVisibility(View.GONE);
		mContentView.findViewById(R.id.btn_video).setVisibility(View.GONE);
		mContentView.findViewById(R.id.btn_pdf).setVisibility(View.GONE);
		mContentView.findViewById(R.id.btn_text).setVisibility(View.GONE);
		mContentView.findViewById(R.id.btn_word).setVisibility(View.GONE);
		mContentView.findViewById(R.id.btn_ppt).setVisibility(View.GONE);
		mContentView.findViewById(R.id.btn_apk).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);

        SharedPreferencesHandler.setStringValues(getActivity(),"GroupOwnerAddress","");
    	SharedPreferencesHandler.setStringValues(getActivity(),"ServerBoolean","");
    	SharedPreferencesHandler.setStringValues(getActivity(),"WiFiClientIp","");
    }

    static Handler handler;
    public static class FileServerAsyncTask extends AsyncTask<String, String, String> {

        private Context mFilecontext;
		private String Extension, Key;
		private File EncryptedFile;
		private long ReceivedFileLength;
		private int PORT;
		private String file_ext;

        public FileServerAsyncTask(Context context, int port) {
            this.mFilecontext = context;

            handler = new Handler();
			this.PORT = port;

			if (mProgressDialog == null)
				mProgressDialog = new ProgressDialog(mFilecontext,
						ProgressDialog.THEME_HOLO_LIGHT);
        }
        

		@Override
        protected String doInBackground(String... params) {
            try {
				CommonMethods.e("File Async task port", "File Async task port-> " + PORT);
				// init handler for progressdialog
				ServerSocket serverSocket = new ServerSocket(PORT);
				
				Log.d(CommonMethods.Tag, "Server: Socket opened");
				Socket client = serverSocket.accept();
				Log.d("Client InetAddress:", "" + client.getInetAddress());

				WiFiClientIp = client.getInetAddress().getHostAddress();
				
				ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
				WiFiTransferModal obj = null;

				String InetAddress;
				try {
					obj = (WiFiTransferModal) ois.readObject();
					InetAddress = obj.getInetAddress();
					if (InetAddress != null && InetAddress.equalsIgnoreCase(FileTransferService.inetaddress)) {
						CommonMethods.e("File Async Group Client Ip", "port-> "+ WiFiClientIp);
						SharedPreferencesHandler.setStringValues(mFilecontext,"WiFiClientIp", WiFiClientIp);
						CommonMethods.e("File Async Group Client Ip from SHAREDPrefrence", "port-> "
								+ SharedPreferencesHandler.getStringValues(mFilecontext, "WiFiClientIp"));
						//set boolean true which identifiy that this device will act as server.
						SharedPreferencesHandler.setStringValues(mFilecontext, "ServerBoolean", "true");
						ois.close(); // close the ObjectOutputStream object
										// after saving
						serverSocket.close();

						return "Demo";
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				final Runnable r = new Runnable() {

					public void run() {
						// TODO Auto-generated method stub
						mProgressDialog.setMessage("Receiving:");
						mProgressDialog.setIndeterminate(false);
						mProgressDialog.setMax(100);
						mProgressDialog.setProgress(0);
						mProgressDialog.setProgressNumberFormat(null);
						mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						mProgressDialog.show();
					}
				};
				handler.post(r);
				Log.e("FileName Got:",obj.getFileName());
				final File f = new File(Environment.getExternalStorageDirectory() + "/" + FolderName + "/" + obj.getFileName());
				File dirs = new File(f.getParent());
				if (!dirs.exists())
					dirs.mkdirs();
				f.createNewFile();
				this.ReceivedFileLength = obj.getFileLength();
				InputStream inputstream = client.getInputStream();
				copyRecievedFile(inputstream, new FileOutputStream(f),ReceivedFileLength);
				ois.close(); // close the ObjectOutputStream object after saving
								// file to storage.
				serverSocket.close();
				this.Extension = obj.getFileName();
				this.EncryptedFile = f;
				file_ext=obj.getFileName();
				Log.e("EXT:",file_ext);
				return f.getAbsolutePath();
			} catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
            	if(!result.equalsIgnoreCase("Demo")){
					Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);

					if(file_ext.contains(".jpg")||file_ext.contains("png"))
                   intent.setDataAndType(Uri.parse("file://" + result), "image/*");
					else if(file_ext.contains(".mp3"))
					intent.setDataAndType(Uri.parse("file://" + result), "audio/*");
					else if(file_ext.contains(".mp4")||file_ext.contains(".mkv"))
					intent.setDataAndType(Uri.parse("file://" + result), "video/*");
					else if(file_ext.contains(".pdf"))
						intent.setDataAndType(Uri.parse("file://" + result), "application/pdf");
					else if(file_ext.contains(".doc")||file_ext.contains(".docx"))
						intent.setDataAndType(Uri.parse("file://" + result), "application/msword");
					else if(file_ext.contains(".ppt")||file_ext.contains(".pptx"))
						intent.setDataAndType(Uri.parse("file://" + result), "application/vnd.ms-powerpoint");
					else if(file_ext.contains(".txt")||file_ext.contains(".xml"))
						intent.setDataAndType(Uri.parse("file://" + result), "plain/text");
					else if(file_ext.contains(".apk"))
						intent.setDataAndType(Uri.parse("file://" + result), "*/*");
					mFilecontext.startActivity(intent);
            	}
            	else{
 					  FileServerAsyncTask FileServerobj = new
					  FileServerAsyncTask(mFilecontext,FileTransferService.PORT); 
					  if(FileServerobj != null) { 
						  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					  FileServerobj.executeOnExecutor (AsyncTask.THREAD_POOL_EXECUTOR, new String[] { null });
					  }
					  else FileServerobj.execute();
					  }
            	}
            }
        }
        @Override
        protected void onPreExecute() {
        	if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(mFilecontext);
			}
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
    	long total = 0;
		long test = 0;
		byte buf[] = new byte[FileTransferService.ByteSize];
		int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
                try {
					total += len;
					if (ActualFilelength > 0) {
						Percentage = (int) ((total * 100) / ActualFilelength);
					}
					mProgressDialog.setProgress(Percentage);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					Percentage = 0;
					ActualFilelength = 0;
				}
            }
            if (mProgressDialog != null) {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }

    public static boolean copyRecievedFile(InputStream inputStream, OutputStream out, Long length) {

		byte buf[] = new byte[FileTransferService.ByteSize];
		byte Decryptedbuf[] = new byte[FileTransferService.ByteSize];
		String Decrypted;
		int len;
		long total = 0;
		int progresspercentage = 0;
		try {
			while ((len = inputStream.read(buf)) != -1) {
				try {
					out.write(buf, 0, len);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					total += len;
					if (length > 0) {
						progresspercentage = (int) ((total * 100) / length);
					}
					mProgressDialog.setProgress(progresspercentage);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					if (mProgressDialog != null) {
						if (mProgressDialog.isShowing()) {
							mProgressDialog.dismiss();
						}
					}
				}
			}
			// dismiss progress after sending
			if (mProgressDialog != null) {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}
			out.close();
			inputStream.close();
		} catch (IOException e) {
			Log.d(WiFiDirectActivity.TAG, e.toString());
			return false;
		}
		return true;
	}
    
    public void showprogress(final String task) {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(getActivity(),
					ProgressDialog.THEME_HOLO_LIGHT);
		}
		Handler handle = new Handler();
		final Runnable send = new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				mProgressDialog.setMessage(task);
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setMax(100);
				mProgressDialog.setProgressNumberFormat(null);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog.show();
			}
		};
		handle.post(send);
	}
    
    public static void DismissProgressDialog() {
		try {
			if (mProgressDialog != null) {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
    
    class firstConnectionMessage extends AsyncTask<String, Void, String> {
		String GroupOwnerAddress = "";
		public firstConnectionMessage(String owner) {
			// TODO Auto-generated constructor stub
			this.GroupOwnerAddress = owner;
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			CommonMethods.e("On first Connect", "On first Connect");
			Intent serviceIntent = new Intent(getActivity(),WiFiClientIPTransferService.class);
			serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);

			if (info.groupOwnerAddress.getHostAddress() != null) {
				serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,info.groupOwnerAddress.getHostAddress());
				serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);
				serviceIntent.putExtra(FileTransferService.inetaddress,FileTransferService.inetaddress);
			}
			getActivity().startService(serviceIntent);
			return "success";
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(result!=null){
				if(result.equalsIgnoreCase("success")){
					CommonMethods.e("On first Connect","On first Connect sent to asynctask");
					ClientCheck = true;
				}
			}
		}
	}
}