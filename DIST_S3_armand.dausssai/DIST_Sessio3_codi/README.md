# Sessió 3 - Remote Procedure Calls
### Requeriments
* gcc-9 (Ubuntu 9.4.0-1ubuntu1~20.04.1) 9.4.0
* libncurses5-dev
* libssl-dev
* SO: Ubuntu 20.04 LTS or greater

### Com executar
Per compilar el codi cal executar la comanda
```bash
make -f Makefile.chat_app
```
Per executar el codi, cal obrir dues terminals com a mínim. La primera executarà el servidor:
```bash
./chat_app_server
```
La terminal N executarà:
```bash
./chat_app_client localhost <username>
```
On <i>username</i> és el nom del usuari que es connecta.

Per tancar el programa, fer CTR+C a cada terminal obert.

### Autor
Armand Daussà Baize (armand.daussai@students.salle.url.edu)