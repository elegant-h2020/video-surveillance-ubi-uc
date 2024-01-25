import csv
import random
import uuid
import time
from datetime import datetime


header = ['colorspace','frame_width','frame_height','node_name','date','tmst_sec','tmst','num_faces','imageID']
camera_names=['cam_1','cam_2','cam_3']


def randomDate(start, end):
    frmt = '%d-%m-%Y %H:%M:%S'

    stime = time.mktime(time.strptime(start, frmt))
    etime = time.mktime(time.strptime(end, frmt))

    ptime = stime + random.random() * (etime - stime)
    dt = datetime.fromtimestamp(time.mktime(time.localtime(ptime)))
    return dt


for i in range(0,6):

    with open('../docker_nes/transc_data_worker1_'+str(i)+'.csv', 'w+', encoding='UTF8', newline='') as f:
        writer = csv.writer(f)

        # write the header
        writer.writerow(header)

        for x in range(10000*(100*i)):
            date =  randomDate("12-04-2022 13:30:00", "12-04-2022 15:00:34")
            tmst= int (time.mktime(date.timetuple()) * 1000)
            tmst_sec= int (time.mktime(date.timetuple()) )
            #tmst= 1558329375000
            randomNum_Faces= random.randint(1, 5)
            random_imageId=  uuid.uuid1()

            #header = ['colorspace','frame_width','frame_height','node_name','date','tmst','num_faces','imageID']
            data=[83,640,360,"cam_1",date,tmst_sec,tmst,randomNum_Faces,random_imageId]
            # write the data
            writer.writerow(data)



    with open('../docker_nes/transc_data_worker2_'+str(i)+'.csv', 'w+', encoding='UTF8', newline='') as f:
        writer = csv.writer(f)

        # write the header
        writer.writerow(header)

        for x in range(1000*(10*i)):
            date =  randomDate("12-04-2022 13:30:00", "12-04-2022 15:00:34")
            tmst= int( time.mktime(date.timetuple()) * 1000 )
            tmst_sec= int (time.mktime(date.timetuple()))
            randomNum_Faces= random.randint(1, 5)
            random_imageId=  uuid.uuid1()
            data=[83,640,360,"cam_2",date,tmst_sec,tmst,randomNum_Faces,random_imageId]
            # write the data
            writer.writerow(data)




