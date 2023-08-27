@echo off
@title RUN ONYX(LOGIN)
java -XX:-OmitStackTraceInFastThrow -server -cp bin;data/lib/netty-3.7.0.Final.jar;data/lib/FileStore.jar;data/lib/minifs_v1.jar; com.rs.LoginLauncher true false
pause