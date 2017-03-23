package nohorjo.roar;

import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import nohorjo.application.App;
import nohorjo.application.MainActivity;
import nohorjo.common.CommonUtils;
import nohorjo.crypto.AESEncryptor;
import nohorjo.crypto.EncryptionException;
import nohorjo.crypto.TimeBasedEncryptor;
import nohorjo.delegation.Action;
import nohorjo.gps.GMaps;
import nohorjo.output.FileOut;
import nohorjo.remote.wordpress.IPHandler;
import nohorjo.socket.SocketClient;

public class RoarComms {
    private static final byte EOT = '\4';
    private TimeBasedEncryptor tbe = new TimeBasedEncryptor();

    public RoarComms() {
        tbe.setAes(new AESEncryptor() {
            @Override
            protected String encodeB64(byte[] input) {
                return new String(Base64.encode(input, 0));
            }

            @Override
            protected byte[] decodeB64(byte[] input) {
                return Base64.decode(input, 0);
            }
        });
    }

    public void forwardMessage(String message) throws Exception {
        if (message.trim().equals("")) {
            return;
        }
        message = "FORWARD:" + message;
        SocketClient client = getClient();
        client.connect();
        client.send(tbe.encrypt(App.key(), message).getBytes());
        client.send(EOT);
        client.close();
        FileOut.println("Forwarded message: " + message);
    }

    private SocketClient getClient() {
        SocketClient client;
        while (true) {
            try {
                client = new SocketClient(IPHandler.getIPAddress(), App.port());
                break;
            } catch (IOException e) {
                FileOut.println(e.getMessage());
                try {
                    IPHandler.reloadIPAddress();
                } catch (Exception e1) {
                    FileOut.printStackTrace(e1);
                }
            }
        }
        client.setActions(new Action() {

            @Override
            public Object run(Object... args) {
                return null;
            }
        }, new Action() {

            @Override
            public Object run(Object... args) {
                return null;
            }
        });
        return client;
    }

    public void getLocations(final int count, final MainActivity mainActivity) {
        new Thread() {
            @Override
            public void run() {
                final SocketClient client = getClient();
                final AtomicBoolean running = new AtomicBoolean(true);
                client.setActions(new Action() {
                    String data = "";

                    @Override
                    public Object run(Object... args) {
                        byte b = (byte) args[0];
                        if (b == EOT) {
                            try {
                                @SuppressWarnings("unchecked")
                                LinkedList<String[]> locations = (LinkedList<String[]>) CommonUtils
                                        .deserialize(Base64.decode(data, 0));
                                FileOut.println("Locations received: " + locations.size());
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mainActivity.clear(R.id.alternateContent);
                                    }
                                });
                                for (final String[] lData : locations) {
                                    mainActivity.runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            mainActivity.addButton(R.id.alternateContent, lData[1],
                                                    new OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            GMaps.open(lData[0], mainActivity);
                                                        }
                                                    });
                                        }
                                    });
                                }
                            } catch (ClassNotFoundException | IOException e) {
                                FileOut.printStackTrace(e);
                            }
                            running.set(false);
                        } else {
                            data += (char) b;
                        }
                        return null;
                    }
                }, new Action() {

                    @Override
                    public Object run(Object... args) {
                        return null;
                    }
                });
                try {
                    client.connect();
                    client.send(tbe.encrypt(App.key(), "GET:" + count).getBytes());
                    client.send(EOT);
                    while (running.get())
                        continue;
                    client.close();
                } catch (IOException | EncryptionException e) {
                    FileOut.printStackTrace(e);
                }
            }

            ;
        }.start();
    }

    public void printTail() {
        new Thread() {
            final AtomicBoolean running = new AtomicBoolean(true);

            @Override
            public void run() {
                super.run();
                SocketClient client = getClient();
                client.setActions(new Action() {
                    String buffer = "";

                    @Override
                    public Object run(Object... objects) {
                        byte b = (byte) objects[0];

                        if (b == EOT) {
                            try {
                                running.set(false);
                                FileOut.println(tbe.decrypt(App.key(), buffer));
                            } catch (EncryptionException e) {
                                FileOut.printStackTrace(e);
                            }
                        } else {
                            buffer += (char) b;
                        }

                        return null;
                    }
                }, new Action() {
                    @Override
                    public Object run(Object... objects) {
                        return null;
                    }
                });
                try {
                    client.connect();
                    client.send(tbe.encrypt(App.key(), "TAIL:T").getBytes());
                    client.send(EOT);
                    while (running.get()) continue;
                    client.close();
                } catch (IOException | EncryptionException e) {
                    FileOut.printStackTrace(e);
                }
            }
        }.start();
    }
}
