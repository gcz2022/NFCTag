package connection.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import connection.json.JSONException;
import connection.json.JSONObject;

public class Client {

    // 新浪服务器地址
    public static final String SERVER_URL = "http://virtualwallet.sinaapp.com/";
    // 本地调试地址
//    public static final String SERVER_URL = "http://localhost/java/virtualwallet/1/";
    private static Client singleton;

    private boolean logined;
    private String username, password;
    private int userId;

    private Client() {
        logined = false;
        username = null;
        password = null;
        userId = -1;
    }

    public static Client getClient() {
        if (singleton == null) {
            singleton = new Client();
        }
        return singleton;
    }

    // test function
    public static void main(String[] args) {
        Client client = Client.getClient();

        Response response = new AsnyRequest() {
            Response getResponse() {
                client.validate("yty", "yty");
                return client.getUserInfo();
            }
        }.post();
        debug(response.helpler.getUserBalance());
        debug(client.isLogined());
//     test1(false);
    }

    // test function
    public static void test1(boolean firstTime) {
        new AsnyRequest() {
            Response getResponse() {
                Client client = Client.getClient();
                if (firstTime) {
                    client.register("t1-admin", "t1-admin");
                }
                debug(client.validate("t1-admin", "t1-admin"));
                if (firstTime) {
                    client.createItem("t1-item1", "t1-item1", "t1-item1 description", 1);
                    client.createItem("t1-item2", "t1-item2", "t1-item2 description", 1);
                }

                client.logout();
                // 切换到顾客
                if (firstTime) client.register("t1-user", "t1-user");
                client.validate("t1-user", "t1-user");
                Response itemInfo = client.getItemInfo("t1-item1");
                if (itemInfo.getResult().equals("success")) {
                    int itemId = itemInfo.json.getJSONObject("itemInfo").getInt("id");
                    client.buyItem(itemId, 1);
                    client.buyItem(itemId, 2);
                }
                client.getBills();
                return null;
            }
        }.post();
    }

    /**
     * 注册一个用户
     *
     * @param username 用户名
     * @param password 密码
     * @return response
     */
    public Response register(String username, String password) {
        try {
            JSONObject content = packActionData("register",
                    new JSONObject().put("username", username).put("password", password));

            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
        }
    }

    /**
     * 验证用户名/密码是否正确
     *
     * @param username 用户名
     * @param password 密码
     * @return response 如果验证通过,将会设置相应的username,password,logined
     */
    public Response validate(String username, String password) {
        try {
            JSONObject content = packActionData("validate",
                    new JSONObject().put("username", username).put("password", password));

            Response response = jsonPost(content, Client.SERVER_URL);
            if (response.getResult().equals("success")) {
                this.logined = true;
                this.username = username;
                this.password = password;
                this.userId = response.json.getInt("userId");
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
        }
    }

    /**
     * 登出
     */
    public void logout() {
        logined = false;
        username = null;
        password = null;
    }

    /**
     * 获取用户的信息
     *
     * @return 返回包含用户信息的Response对象, 如果用户不存在或发生错误, 返回对应错误信息
     */
    public Response getUserInfo() {
        if (!logined) {
            return Response.Prefab.needValidate();
        }
        try {
            JSONObject content = packActionData("getUserInfo",
                    addUserInfo(new JSONObject()));

            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
        }
    }

    public Response getBills() {
        if (!logined) {
            return Response.Prefab.needValidate();
        }
        try {
            JSONObject content = packActionData("getBills", addUserInfo(new JSONObject()));
            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
        }
    }

    /**
     * 创建物品单
     *
     * @return response
     */
    public Response createItem(String rawVal, String name, String description, int price) {
        if (!logined) {
            return Response.Prefab.needValidate();
        }
        JSONObject data = addUserInfo(new JSONObject()).put("rawVal", rawVal);
        data.put("name", name).put("description", description).put("price", price);
        JSONObject content = packActionData("createItem", data);

        try {
            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
        }
    }

    /**
     * 购买物品
     *
     * @param itemId 购买的商品id
     * @param amount 购买数量
     * @return response
     */
    public Response buyItem(int itemId, int amount) {
        if (!logined) {
            return Response.Prefab.needValidate();
        }
        JSONObject data = addUserInfo(new JSONObject());
        data.put("itemId", itemId).put("amount", amount);
        JSONObject content = packActionData("buyItem", data);

        try {
            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
        }

    }

    /**
     * 获取物品单的信息
     */
    public Response getItemInfo(String rawVal) {
        JSONObject data = new JSONObject().put("rawVal", rawVal);
        JSONObject content = packActionData("getItemInfo", data);
        try {
            return jsonPost(content, Client.SERVER_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return Response.Prefab.connectionError();
        }
    }

    public boolean isLogined() {
        return logined;
    }

    public String getUsername() {
        if (logined)
            return username;
        else
            return null;
    }

    public String getPassword() {
        if (logined)
            return password;
        else
            return null;
    }

    public int getUserId() {
        if (logined)
            return userId;
        else
            return -1;
    }

    private JSONObject addUserInfo(JSONObject data) {
        if (logined) {
            data.put("username", username).put("password", password).put("userId", userId);
        }
        return data;
    }

    // test function
    public String toString() {
        return "logined:" + logined + "\nuserId:" +
                userId + "\nusername:" + username + "\npassword:" + password;
    }

    // debug function
    static void debugToFile(String content) {
        File file = new File("C:\\Users\\sfc84\\.IdeaIC14\\config\\scratches\\scratch");
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // debug function
    public static void debug(Object... objects) {
        String seprator = "";
        for (Object object : objects) {
            if (object != null)
                System.out.print(seprator + object.toString());
            else
                System.out.print("null ");
            seprator = " ";
        }
        System.out.println();
    }

    private Response jsonPost(JSONObject jsonObject, String serverUrl) throws IOException {
        return post(jsonEncode(jsonObject), serverUrl);
    }

    private Response post(String content, String serverUrl) throws IOException {
        URL url = new URL(serverUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);

        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        connection.setRequestProperty("Content-type",
                "application/x-www-form-urlencoded");
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        // connection.connect(); // connection.setDoOutput(true)将调用connect(),故connect()可以省略
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.writeBytes(content);
        out.flush();
        out.close();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String result = readAllFromReader(reader);

        reader.close();
        connection.disconnect();

//         TODO
//        debugToFile(result);
        return new Response(result);
    }

    private static JSONObject packActionData(String action, JSONObject data) {
        return new JSONObject().put("action", action).put("data", data);
    }

    private static String jsonEncode(JSONObject jsonObject) throws IOException {
        Iterator<String> keys = jsonObject.keys();
        String key, seprator = "";
        Object value;
        StringBuilder result = new StringBuilder();
        while (keys.hasNext()) {
            key = keys.next();
            value = jsonObject.get(key);
            result.append(seprator).append(urlEncode(key))
                    .append("=").append(urlEncode(value.toString()));
            seprator = "&";
        }
        return result.toString();
    }

    private static String urlEncode(String string) throws IOException {
        return URLEncoder.encode(string, "utf-8");
    }

    private String readAllFromReader(BufferedReader reader) {
        String line;
        StringBuilder builder = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public abstract static class AsnyRequest implements Runnable {
        private Response response;

        @Override
        public void run() {
            response = getResponse();
        }

        abstract Response getResponse();

        /**
         * 发送一个异步请求
         *
         * @return 请求对应的响应
         */
        public Response post() {
            Thread thread = new Thread(this);
            try {
                thread.start();
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    public static class Response {
        public static final boolean autoDebug = true;
        public Helpler helpler;
        private String result, errorMsg, rawString;
        public JSONObject json;

        static class Prefab {
            static Response needValidate() {
                return new Response(new JSONObject().put("result", "error")
                        .put("errorMsg", "Call validate() first!").toString());
            }

            static Response connectionError() {
                return new Response(new JSONObject().put("result", "error")
                        .put("errorMsg", "Cannot connect to server.").toString());
            }

            static Response needLogout() {
                return new Response(new JSONObject().put("result", "error")
                        .put("errorMsg", "You are alread validated, call logout() before call validate().").toString());
            }
        }

        public class Helpler {
            /**
             * 获取用户的余额
             */
            public int getUserBalance() {
                return json.getJSONObject("userInfo").getInt("balance");
            }
        }

        public Response(String string) {
            rawString = string;
            try {
                json = new JSONObject(string);
                result = json.getString("result");
                if (result.equals("error")) {
                    errorMsg = json.getString("errorMsg");
                }
            } catch (JSONException e) {
                json = null;
                result = "error";
                errorMsg = "Server responds a non-JSON string or an invalid JSON string.";
            }
            if (autoDebug)
                Client.debug(this);
            helpler = new Helpler();
        }

        public String getRawString() {
            return rawString;
        }

        public String getResult() {
            return result;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public String toString() {
            if (json != null)
                return json.toString(4);
            else
                return rawString;
        }
    }

}
