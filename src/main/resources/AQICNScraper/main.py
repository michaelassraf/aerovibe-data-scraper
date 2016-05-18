from threading import Thread
from Data_Scraper import Data_Scraper
from Sensors_Scraper import Sensors_Scraper
import os,sys, global_values

base_path = sys.argv[1]
#base_path = 'C:\temp\'

test = Sensors_Scraper("http://aqicn.org/city/all/", 
                       {'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11',
       'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
       'Accept-Charset': 'ISO-8859-1,utf-8;q=0.7,*;q=0.3',
       'Accept-Encoding': 'none',
       'Accept-Language': 'en-US,en;q=0.8',
       'Connection': 'keep-alive'})
data = test.get_data()

def get_data(state, urls):
    for url in urls:
        try:
            test = Data_Scraper(url, {'User-Agent': 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11',
           'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
           'Accept-Charset': 'ISO-8859-1,utf-8;q=0.7,*;q=0.3',
           'Accept-Encoding': 'none',
           'Accept-Language': 'en-US,en;q=0.8',
           'Connection': 'keep-alive'})
            data2 = test.get_data()
            if os.path.isfile(state+".txt"):
                with open(base_path+state+".txt", "a") as myfile:
                    myfile.write(","+str(data2))
            else:
                with open(base_path+state+".txt", "a") as myfile:
                    myfile.write(str(data2))
            if global_values.debug:
                print key
        except Exception, e:
            if global_values.debug:
                print str(e) + " basa " + url
    global_values.thread_counter -= 1
    if global_values.debug:
            print global_values.thread_counter
    if global_values.thread_counter == 0:
        sys.exit()

if __name__ == "__main__":
    for key, value in data.items():
        global_values.thread_counter += 1
        if global_values.debug:
            print global_values.thread_counter
        thread = Thread(target = get_data, args = (key, value))
        thread.start()
        if global_values.debug:
            print "Start " + key
