/**
 * Created by tao on 6/24/15.
 */
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import java.lang.String;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


public class MyHttpClient {
    public static void main(String[] args) {
        new MainFrame("86-Server-POST", 480, 800);
    }
}

class MainFrame extends JFrame {
    public static String labelText[] = {"选择服务器", "服务器URL", "Form表单(json格式)[当前测试账号:flyfish]",
            "请求结果(json格式)"};
    public static int labelPos[][] = {{20, 0, 100, 30}, {20, 90, 200, 30}, {20, 160, 300, 30}, {20, 370, 200, 30}};
    public static String serverText[] = {"Main Server", "Game Server", "Chat Server"};

    public final String hint = "{\"cmd\": \"101\", \"username\": \"flyfish\", \"password\": \"123456\", " +
            "\"reqid\": \"999\"}";

    public JComboBox serverChoice = null;
    public JTextField serverURL = null;

    public JTextArea sendArea = null;
    public JTextArea contentArea = null;

    public JButton sendButton = null;

    public JScrollPane sendScrollBar = null;
    public JScrollPane contentScrollBar = null;

    public boolean canClick = true;

    public enum CHOICES{
        MAIN_SERVER, GAME_SERVER, CHAT_SERVER
    }

    public CHOICES curChoice = CHOICES.MAIN_SERVER;

    public Cookie cookies[] = null;
    public static HttpClient httpClient = null;

    public MainFrame(String title, int width, int height) {
        super();
        setTitle(title);
        setSize(width, height);
        this.setResizable(false);
        add(new MainPanel());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public static synchronized HttpClient getInstance() {
        if (httpClient == null) {
            httpClient = new HttpClient();
        }
        return httpClient;
    }

    public void setCookie(CHOICES choice) {
        // 主动登录一次, 记录cookie
        httpClient = getInstance();
        String url = "http://192.168.1.86:10100/domaincmd";
        NameValuePair nameValuePairs[] = null;
        // 登录main服务器
        if (choice == CHOICES.MAIN_SERVER) {
            nameValuePairs = new NameValuePair[3];
            nameValuePairs[0] = new NameValuePair("cmd", "101");
            nameValuePairs[1] = new NameValuePair("username", "flyfish");
            nameValuePairs[2] = new NameValuePair("password", "123456");
        }
        // 其余情况登录game服务器
        else {
            url = "http://192.168.1.86:10101/dogamecmd";
            nameValuePairs = new NameValuePair[2];
            nameValuePairs[0] = new NameValuePair("cmd", "201");
            nameValuePairs[1] = new NameValuePair("username", "flyfish");
        }
        if (nameValuePairs == null || nameValuePairs.length == 0) return;
        PostMethod method = new PostMethod(url);
        method.setRequestBody(nameValuePairs);
        try {
            httpClient.executeMethod(method);
            cookies = httpClient.getState().getCookies();
        } catch (HttpException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }

    class myThread extends Thread {
        public void run() {
            // 获取参数
            ObjectMapper objectMapper = new ObjectMapper();
            JsonGenerator jsonGenerator = null;
            NameValuePair[] nameValuePairs = null;
            try {
                jsonGenerator = objectMapper.getJsonFactory().createJsonGenerator(System.out, JsonEncoding.UTF8);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                // 简单处理输入的文本
                String str0 = sendArea.getText();
                str0 = str0.replaceAll("'", "\"");
                Map<String, String> maps = objectMapper.readValue(str0, Map.class);
                Set<String> key = maps.keySet();
                Iterator<String> iter0 = key.iterator();
                int size = maps.size();
                if (size > 0) {
                    nameValuePairs = new NameValuePair[size];
                }
                int index = 0;
                NameValuePair nameValuePair = null;
                while (iter0.hasNext()) {
                    String field = iter0.next();
                    maps.get(field);
                    nameValuePair = new NameValuePair(field, maps.get(field).toString());
                    nameValuePairs[index] = nameValuePair;
                    index++;
                }
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 获取cookies
            setCookie(curChoice);

            // 获取服务器配置信息
            String url = serverURL.getText();
            httpClient = getInstance();
            httpClient.getState().addCookies(cookies);
            PostMethod method = new PostMethod(url);
            if (nameValuePairs == null || nameValuePairs.length == 0) return;
            method.setRequestBody(nameValuePairs);
            try {
                httpClient.executeMethod(method);
                contentArea.setText(method.getResponseBodyAsString());
                canClick = true;
            } catch (HttpException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            } catch (IOException ex) {
                // TODO Auto-generated catch block
                ex.printStackTrace();
            }

            try {
                if (jsonGenerator != null) {
                    jsonGenerator.flush();
                }
                if (!jsonGenerator.isClosed()) {
                    jsonGenerator.close();
                }
                jsonGenerator = null;
                objectMapper = null;
                System.gc();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    自定义文本控件, 实现输入提示功能
     */
    class HintTextArea extends JTextArea implements FocusListener {
        private final String hint;
        private boolean showingHint;

        public HintTextArea(final String hint, int width, int height) {
            super(hint, width, height);
            this.hint = hint;
            this.showingHint = true;
            super.addFocusListener(this);
        }

        @Override
        public void focusGained(FocusEvent event) {
            if (!this.getText().isEmpty()) return;
            super.setText("");
            showingHint = false;
        }

        @Override
        public void focusLost(FocusEvent event) {
            if (this.getText().isEmpty()) {
                super.setText(hint);
                showingHint = true;
            }
        }

        @Override
        public String getText() {
            return showingHint ? "" : super.getText();
        }
    }

    class MainPanel extends JPanel {
        public MainPanel(){
            super();
            JLabel label = null;
            for (int i=0;i<labelText.length;i++) {
                label = new JLabel(labelText[i]);
                label.setBounds(labelPos[i][0], labelPos[i][1], labelPos[i][2], labelPos[i][3]);
                this.add(label);
            }

            serverChoice = new JComboBox(serverText);
            serverChoice.setBounds(20, 50, 200, 30);
            serverChoice.setSelectedIndex(0);
            serverChoice.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    String choice = e.getItem().toString();
                    if (choice.equals(serverText[0])){
                        curChoice = CHOICES.MAIN_SERVER;
                        serverURL.setText("http://192.168.1.86:10100/domaincmd");
                    }
                    else if (choice.equals(serverText[1])) {
                        curChoice = CHOICES.GAME_SERVER;
                        serverURL.setText("http://192.168.1.86:10101/dogamecmd");
                    }
                    else {
                        curChoice = CHOICES.CHAT_SERVER;
                        serverURL.setText("http://192.168.1.86:10102/dochatcmd");
                    }
                }
            });
            this.add(serverChoice);

            serverURL = new JTextField("http://192.168.1.86:10100/domaincmd");
            serverURL.setBounds(20, 120, 400, 30);
            serverURL.setEditable(false);
            this.add(serverURL);

            // Hint

            sendArea = new HintTextArea(hint, 100, 50);
            sendArea.setLineWrap(true);
            contentArea = new JTextArea(100, 50);
            contentArea.setLineWrap(true);
            contentArea.setEditable(false);
            sendScrollBar = new JScrollPane(sendArea);
            contentScrollBar = new JScrollPane(contentArea);

            sendScrollBar.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            sendScrollBar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            contentScrollBar.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            contentScrollBar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            sendScrollBar.setBounds(20, 210, 450, 150);
            this.add(sendScrollBar);

            contentScrollBar.setBounds(20, 410, 450, 300);
            this.add(contentScrollBar);

            sendButton = new JButton("提交");
            sendButton.setBounds(380, 720, 72, 30);
            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!canClick) return;
                    canClick = false;
                    if (sendArea.getText() == "" || sendArea.getText().length() == 0) {
                        JOptionPane.showMessageDialog(null, "表单不能为空!", "错误提示",
                                JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    new myThread().start();
                }
            });
            this.add(sendButton);

            this.setLayout(null);
            this.setVisible(true);
        }
    }
}
