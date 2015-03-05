#!/usr/bin/ruby
require 'socket'
require 'pty'

#params for serial port  
port_str = "/dev/ttyACM0"  #may be different for you  
baud_rate = 115200  


PTY.spawn("/usr/bin/cu", "-l", "/dev/ttyACM0", "-s" ,"115200") do |output,input,pid|
	sleep 1
	input.write "0:1\n"
	sleep 1
	input.write "0:0\n"
	sleep 1
	input.write "0:1\n"
	sleep 1
	input.write "0:0\n"
	sleep 1

server = TCPServer.open(30000)


p "in server code"
loop {
	client = server.accept
	print "Clinet accepted\n"
	s = client.read 24
	
#	i = 0;
#	s.each_char(){|v|
#		print "%d\t%d\n" % [ i , v.ord ]
#		i = i +1;
#	}
	p s.unpack('H*' );
	cmd = s[16].ord-48
	param= s[17].ord

	p cmd
	p param
	input.write cmd
	input.write ":"
	input.write param
	input.write "\n"
	client.close
}
end
