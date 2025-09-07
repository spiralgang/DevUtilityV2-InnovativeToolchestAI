```md
# 100 Linux Network & Wi‑Fi Tools — consolidated, deduplicated

This is a clean reference list (100 entries) of common and advanced network tools you can use on Ubuntu Lite / Termux / Debian / Android shells. Many entries include the canonical command example.

1. ifconfig — display interfaces (deprecated). Example: ifconfig
2. ip — modern netlink utility. Example: ip address show
3. ping — ICMP reachability. ping google.com
4. traceroute / tracepath — show hops. traceroute google.com
5. netstat — legacy sockets/route; use ss. netstat -a
6. ss — socket statistics. ss -a
7. dig — DNS queries. dig google.com
8. nslookup — DNS lookup. nslookup google.com
9. host — DNS lookup. host google.com
10. route — legacy routing table. route -n
11. ip route — modern routing. ip route show
12. iwconfig — wireless config. iwconfig
13. iw — wireless tools. iw dev wlan0 link
14. nmap — network scanner. nmap -sP 192.168.1.0/24
15. tcpdump — packet capture. sudo tcpdump -i eth0
16. tshark — CLI Wireshark engine. sudo tshark -i eth0
17. wget — download files. wget https://example.com/file
18. curl — request HTTP. curl -I https://example.com
19. ssh — secure shell. ssh user@host
20. scp / rsync — file copy (scp, rsync -av)
21. iperf / iperf3 — bandwidth tests. iperf3 -s / iperf3 -c <server>
22. iftop — live bandwidth per connection. sudo iftop
23. nethogs — per-process bandwidth. sudo nethogs
24. iptraf — LAN monitor. sudo iptraf
25. mtr — traceroute + ping. mtr google.com
26. arp / arp-scan — ARP table / scanning. arp -a / sudo arp-scan --localnet
27. ethtool — NIC settings. sudo ethtool eth0
28. ethtool -S — NIC stats
29. nmcli — NetworkManager CLI. nmcli device status
30. connmanctl — ConnMan CLI (embedded)
31. systemd-networkdctl — systemd-networkd tools
32. netplan — Ubuntu network config (yaml)
33. hostapd — software AP daemon. sudo hostapd /etc/hostapd.conf
34. dnsmasq — lightweight DNS/DHCP
35. dhclient — DHCP client. sudo dhclient wlan0
36. iwlist — older wifi scanning
37. rfkill — block/unblock radios. rfkill list
38. wpa_supplicant — wifi authentication daemon
39. nm-connection-editor — GUI editors
40. iptables — legacy firewall. sudo iptables -L
41. nftables / nft — modern firewall. sudo nft list ruleset
42. ufw — uncomplicated firewall wrapper. sudo ufw status
43. firewalld — RHEL wrapper
44. ipset — manage IP sets for iptables/nft
45. tc / iproute2 traffic control — qdisc shaping. sudo tc qdisc add dev eth0 root netem delay 100ms
46. ssldump — SSL/TLS debugging
47. openssl s_client — TLS testing. openssl s_client -connect host:443
48. socat — socket relay / proxy
49. ncat / netcat (nc) — raw TCP/UDP I/O
50. ngrep — network grep (payload)
51. ping6 / ip -6 — IPv6 tools
52. traceroute6 — IPv6 traceroute
53. tcpflow — reconstruct TCP flows
54. telnet — debug TCP (telnet host port)
55. curl + --interface — bind to interface
56. speedtest-cli — internet speed tool
57. mosh — mobile shell resilient to roaming
58. autossh — persistent SSH tunnels
59. sshuttle — VPN-over-SSH for simple routing
60. openvpn / wg-quick (WireGuard) — VPN clients
61. strongSwan / libreswan — IPsec
62. stunnel — TLS wrapping
63. haproxy / nginx — reverse proxies / load balancers
64. keepalived — VRRP / HA
65. docker network / docker-compose networking tools
66. kubectl / kube-proxy / kubeadm networking
67. ipvsadm — Linux Virtual Server IPVS
68. conntrack — kernel connection tracking
69. ssldump / testssl.sh — TLS scanning
70. zmap / zgrab — Internet-scale scanning (use responsibly)
71. masscan — high-speed port scanning (use legally)
72. traceroute-ng / tracepath alternative
73. bmon — bandwidth monitor (ncurses)
74. nload — network throughput monitor
75. vnstat — persistent network usage stats
76. collectd / prometheus node_exporter — metrics exporters
77. grafana — visual dashboards for metrics
78. tcpick — packet sniffer and analyzer
79. p0f — passive OS fingerprinting
80. arping — ARP ping
81. netdiscover — simple host discovery
82. sslyze — TLS scan and cert checks
83. bro/zeek — network security monitoring
84. suricata — IDS / IPS
85. snort — IDS
86. ntopng — network traffic analytics
87. broctl / zeekctl — Zeek control tools
88. wireguard-go — userspace WireGuard
89. nftables-restore / iptables-restore
90. iptables-save / nft list ruleset (save/restore)
91. ip rule / ip netns — network namespaces and policy routing
92. ip tuntap / tunctl — TAP/TUN devices
93. socat + systemd socket activation combos
94. tcptraceroute — TCP traceroute
95. hping3 — custom TCP/UDP/ICMP packet forge (use responsibly)
96. arpwatch — ARP monitoring & notifications
97. iperf3 with JSON output for CI
98. ncat (nmap-project) for scripting
99. nftables sets + maps (high performance)
100. policycoreutils / auditd (audit network-relevant events)

Notes
- Use modern ip/iproute2, ss, nft rather than legacy tools for scripting and reliability.
- On Android (Termux), some tools may be missing; prefer busybox/ip or install packages (pkg install iproute2 nmap tcpdump).
- For Wi‑Fi toggles on Android, use nmcli when running a full Linux userspace (Ubuntu-Lite) or Android-specific APIs (no root -> use app UI).
- Always test dangerous tools (masscan, zmap, hping3) on your own network or with permission.