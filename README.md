# Overview
This plugin for working with Kaoinc 1S device and implements core messenger functionality such as chat, file transfer and audio calls.

## Development Environment
* Install [Rust](https://www.rust-lang.org/tools/install)
* Add rust toolchains
```
rustup target add armv7-linux-androideabi
rustup target add i686-linux-android
rustup target add aarch64-linux-android
```
* Update local.properties file
```
rust.rustcCommand=$HOME/.cargo/bin/rustc
rust.cargoCommand=$HOME/.cargo/bin/cargo
rust.pythonCommand=/opt/homebrew/bin/python3
```
Place the reticulum-rs repository (currently private) at the same directory level as this repository.

## API
KaonicLib: Bridge class used internally to connect the Rust library with the JNI layer.

KaonicCommunicationManager: Class that provides communication interface for Android applications. Providing integration with the KaonicLib, manages connections and data transmission.

KaonicEventListener: The intefrace that defines callback methods for handling various events such as message updates, connection changes, and user actions within the Kaonic communication system.

## Usage
* Create a singlew instance of KaonicCommunicationManager in your app
* Set event listener:
```
kaonicCommunicationHandler.setEventListener(this)
```
* Get secret key 
```
kaonicCommunicationHandler.generateSecret()
```
* Start communication with selected protocol
    * For TCPIP communication - run this command from reticulum-rs directory: ````cargo run --example tcp-server````
    ```
    kaonicCommunicationHandler.start(
                secretKey,
               ConnectionConfig(
                   ConnectionContact(MY_NAME), arrayListOf(
                    Connection(
                            ConnectionType
                                .TcpClient, ConnectionInfo(IP_ADDRESS)
                        )
                    )
                )
            )
    ```

For additional details, please refer to the ```kaonic-sample```.