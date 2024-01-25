#FROM nebulastream/nes-executable-image:0.2.16
FROM nebulastream/nes-executable-image:0.4.36


ADD entrypoint.sh /
ADD start_workers.sh /
ADD start_workers_1.sh /
ADD start_workers_2.sh /
ADD transc_data_* /opt/local/nebula-stream/


ENTRYPOINT ["sh", "/entrypoint.sh"]
