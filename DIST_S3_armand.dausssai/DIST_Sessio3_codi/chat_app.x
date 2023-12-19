program CHAT_APP{
	version VERSION_1 {
		int writeMsg(string)=1;
		string getChat(void)=2;
	} = 1;

}=0x1234ffff;