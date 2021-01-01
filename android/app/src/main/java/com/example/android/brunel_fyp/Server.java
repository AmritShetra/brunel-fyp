package com.example.android.brunel_fyp;

import io.github.cdimascio.dotenv.Dotenv;

class Server {

    // https://github.com/cdimascio/dotenv-java
    private static Dotenv dotenv = Dotenv.configure()
            .directory("./assets")
            .filename("env")
            .load();

    static String getAddress() {
        return dotenv.get("SERVER_ADDRESS");
    }
}
