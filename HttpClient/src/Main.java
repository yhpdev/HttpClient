import java.awt.event.*;
import javax.swing.*;

import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


public class Main {
    public static void main(String[] args) {
        new MainFrame("86-Server-POST", 480, 800);
    }
}

class MainFrame extends JFrame {
    public static String labelText[] = {"Form表单(json格式)", "请求结果(json格式)"};
    public static int labelPos[][] = {{20, 0, 200, 50}, {20, 260, 200, 50}};

    public JTextArea sendArea = null;
    public JTextArea contentArea = null;

    public JButton sendButton = null;

    public JScrollPane sendScrollBar = null;
    public JScrollPane contentScrollBar = null;

    public boolean canClick = true;

    public MainFrame(String title, int width, int height) {
        super();
        setTitle(title);
        setSize(width, height);
        this.setResizable(false);
        add(new MainPanel());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
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
                Map<String, String> maps = objectMapper.readValue(sendArea.getText(), Map.class);
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

            String url = "http://192.168.1.86:10100/domaincmd";
            HttpClient httpClient = new HttpClient();
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

    class MainPanel extends JPanel {
        public MainPanel(){
            super();
            JLabel label = null;
            for (int i=0;i<2;i++) {
                label = new JLabel(labelText[i]);
                label.setBounds(labelPos[i][0], labelPos[i][1], labelPos[i][2], labelPos[i][3]);
                this.add(label);
            }

            sendArea = new JTextArea(100, 50);
            sendArea.setLineWrap(true);
            contentArea = new JTextArea(100, 50);
            contentArea.setLineWrap(true);
            sendScrollBar = new JScrollPane(sendArea);
            contentScrollBar = new JScrollPane(contentArea);

            sendScrollBar.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            sendScrollBar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            contentScrollBar.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            contentScrollBar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            sendScrollBar.setBounds(20, 50, 450, 200);
            this.add(sendScrollBar);

            contentScrollBar.setBounds(20, 320, 450, 360);
            this.add(contentScrollBar);

            sendButton = new JButton("提交");
            sendButton.setBounds(380, 720, 80, 40);
            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!canClick) return;
                    canClick = false;
                    if (sendArea.getText() == "" || sendArea.getText().length() == 0) return;
                    new myThread().start();
                }
            });
            this.add(sendButton);

            this.setLayout(null);
            this.setVisible(true);
        }
    }
}
