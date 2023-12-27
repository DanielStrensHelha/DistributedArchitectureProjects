Command to compile : 
gcc -o chatapp_server chatapp_svc.c chatapp_xdr.c -lnsl -lpthread -lncurses -ltirpc -I /usr/include/tirpc/
gcc -o chatapp_client chatapp_client.c chatapp_xdr.c -lnsl -lpthread -lncurses -ltirpc -I /usr/include/tirpc/
gcc -o chatapp_clnt chatapp_clnt.c chatapp_xdr.c -lnsl -lpthread -lncurses -ltirpc -I /usr/include/tirpc/