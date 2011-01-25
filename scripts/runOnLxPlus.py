#!/usr/bin/python
# Gets a path of a testing set, divides it into portions,
# uploads parts to a temp directory on lxplus, executes the
# extraction process on a number of nodes, retrieves output and results



# for the moment, we are submitting to only one lxplus node

import pexpect
import getpass


username = raw_input("Username: ")
password = getpass.getpass("Password: ")

con = pxssh.pxssh()
con.login("lxplus243", username, password, login_timeout=10000)

con.sendline("uptime")
con.prompt()
print con.before
