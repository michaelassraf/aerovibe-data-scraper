import urllib2,string,global_values,re,json
from bs4 import BeautifulSoup

class Data_Scraper:
    def __init__(self, url, user_agent, proxy_ip = None, proxy_port = None):
        self.url = url
        self.user_agent = user_agent
        self.proxy_ip = proxy_ip
        self.proxy_port = proxy_port
        if proxy_ip is not None:
            print 'todo'
        else:
            self.req = urllib2.Request(self.url, headers=self.user_agent)
        self.raw_data = urllib2.urlopen(self.req)
            
    def get_data(self):
        try:
            data_dict = {}
            
            #------------------------
            #
            # Data extraction
            #
            #------------------------
        
            soup = BeautifulSoup(urllib2.urlopen(self.req).read())
            
            pm25arr = soup.find_all(id="cur_pm25")
            if pm25arr:
                data_dict['PM2_5'] = pm25arr[0].text
                
            pm10arr = soup.find_all(id="cur_pm10")
            if pm10arr:
                data_dict['PM10'] = pm10arr[0].text
                
            o3arr = soup.find_all(id="cur_o3")
            if o3arr:
                data_dict['O3'] = o3arr[0].text
            
            no2arr = soup.find_all(id="cur_no2")
            if no2arr:
                data_dict['No2'] = no2arr[0].text
                
            so2arr = soup.find_all(id="cur_so2")
            if so2arr:
                data_dict['So2'] = so2arr[0].text
                
            temp = soup.find_all(id="cur_t")
            if temp:
                data_dict['Temperature'] = temp[0].text
                
            pressure = soup.find_all(id="cur_p")
            if pressure:
                data_dict['Pressure'] = pressure[0].text
            
            humidity = soup.find_all(id="cur_h")
            if humidity:
                data_dict['Humidity'] = humidity[0].text
            
            wind = soup.find_all(id="cur_w")
            if wind:
                data_dict['Wind'] = wind[0].text
            
            name = re.match('(.*)\Real-time', soup.find(id="aqiwgttitle2").text).group(1) # Get the sensor name
            name = name[1:]
            name = name [:-1]
            data_dict['Name'] = name
        
            #---------------------------------
            #
            # Coordinates and time extraction
            #
            #--------------------------------
        
            script = soup.find('script', text=re.compile('mapCityData'))
            text = script.text
            temp_text = re.search('\{(.*?)'+name+'(.*?)\}', text)
            temp_text_time2 = string.split(temp_text.group(1), "aqi")
            coordinates = re.search('\"g\"\:\[(.*?)\]', temp_text.group(2))
            time = re.search('\"utime\"\:(.*?)\,', temp_text_time2[-1])
            data_dict['Coordinates'] = coordinates.group(1)
            data_dict['Time'] = time.group(1)
            return json.dumps(data_dict, ensure_ascii=False)
        except urllib2.HTTPError, e:
            if global_values.debug:
                print e.fp.read()
    
    
