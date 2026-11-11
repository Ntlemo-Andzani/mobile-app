#!/bin/sh
set -e

# Make Tomcat's HTTP connector listen on the port the host assigns (default 8080).
PORT="${PORT:-8080}"
sed -i "s/port=\"8080\"/port=\"${PORT}\"/" /usr/local/tomcat/conf/server.xml

exec catalina.sh run
