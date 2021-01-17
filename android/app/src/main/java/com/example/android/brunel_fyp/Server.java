package com.example.android.brunel_fyp;

import io.github.cdimascio.dotenv.Dotenv;

class Server {

    private static Dotenv dotenv = Dotenv.configure()
            .directory("./assets")
            .filename("env")
            .load();

    private static String address = dotenv.get("SERVER_ADDRESS");

    // Sends back the address (e.g. localhost:5000) + an extension (e.g. /user/1/)
    static String route(String extension) {
        return address + extension;
    }
}
