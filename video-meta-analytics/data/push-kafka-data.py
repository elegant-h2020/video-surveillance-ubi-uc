# This is a sample Python script.

# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.
import csv
import json
from  datetime import  datetime
from kafka import KafkaProducer, KafkaClient
import threading


def print_hi(name):
    # Use a breakpoint in the code line below to debug your script.
    KAFKA_HOST = "212.101.173.176:29092"  # Or the address you want
    producer = KafkaProducer(bootstrap_servers=KAFKA_HOST)
    topic = "crowd-estimate"


    print(f'Hi, {name}')  # Press Ctrl+F8 to toggle the breakpoint.
    with open("/home/plakic/playground/Ubitech/Elegant/video_surveillance_and_metadata_analytics/query_output/output_estimate_query.csv") as file_obj:
        reader_obj = csv.reader(file_obj)
        next(reader_obj)
        for row in reader_obj:
            tmst = datetime.fromtimestamp(int(row[0])/1000)
            tmstp = datetime.fromtimestamp(int(row[1])/1000)

            thisdict = {
                "tmst": tmst.strftime("%m/%d/%Y, %H:%M:%S"),
                "tmstp": tmstp.strftime("%m/%d/%Y, %H:%M:%S"),
                "node_name": row[2],
                "count": row[3]
            }
            print(thisdict)
            json_dict= json.dumps(thisdict)

            producer.send(topic, json_dict.encode('utf-8'))

# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    print_hi('PyCharm')

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
