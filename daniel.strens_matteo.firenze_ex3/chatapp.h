/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#ifndef _CHATAPP_H_RPCGEN
#define _CHATAPP_H_RPCGEN

#include <rpc/rpc.h>


#ifdef __cplusplus
extern "C" {
#endif


struct message {
	char *sender;
	char *content;
};
typedef struct message message;

struct chat_history {
	struct {
		u_int messages_len;
		message *messages_val;
	} messages;
	int count;
};
typedef struct chat_history chat_history;

#define CHATAPP 0x3a3afeeb
#define VER1 1

#if defined(__STDC__) || defined(__cplusplus)
#define get_chat_history 1
extern  chat_history * get_chat_history_1(void *, CLIENT *);
extern  chat_history * get_chat_history_1_svc(void *, struct svc_req *);
#define send_message 2
extern  int * send_message_1(message *, CLIENT *);
extern  int * send_message_1_svc(message *, struct svc_req *);
extern int chatapp_1_freeresult (SVCXPRT *, xdrproc_t, caddr_t);

#else /* K&R C */
#define get_chat_history 1
extern  chat_history * get_chat_history_1();
extern  chat_history * get_chat_history_1_svc();
#define send_message 2
extern  int * send_message_1();
extern  int * send_message_1_svc();
extern int chatapp_1_freeresult ();
#endif /* K&R C */

/* the xdr functions */

#if defined(__STDC__) || defined(__cplusplus)
extern  bool_t xdr_message (XDR *, message*);
extern  bool_t xdr_chat_history (XDR *, chat_history*);

#else /* K&R C */
extern bool_t xdr_message ();
extern bool_t xdr_chat_history ();

#endif /* K&R C */

#ifdef __cplusplus
}
#endif

#endif /* !_CHATAPP_H_RPCGEN */