export TAG=3.2.1
export PORT=$1
export PASSWORD=$2
apt-get update && \
apt-get install git -y && \
cd /usr/bin && \
rm -rf ./shadowsocksr /etc/systemd/system/shadowsocksR.servic && \
git clone https://github.com/shadowsocksrr/shadowsocksr.git && \
cd shadowsocksr/shadowsocks && \
git checkout $TAG && \
echo password=$PASSWORD && \
echo PORT=$PORT && \
cat > /etc/systemd/system/shadowsocksR.service <<EOF
[Unit]
Description=ShadowsocksR Server Service
Requires=network.target
After=network.target
[Service]
Type=forking
User=root
Restart=always
ExecStart=/usr/bin/python /usr/bin/shadowsocksr/shadowsocks/server.py -p $PORT -k $PASSWORD -m aes-128-cfb -O auth_aes128_md5 -o tls1.2_ticket_auth_compatible
[Install]
WantedBy=multi-user.target
EOF
systemctl daemon-reload
systemctl enable shadowsocksR.service
systemctl start shadowsocksR.service