#!/bin/bash

HOST=science-docker
REMOTE_DIR=/var/www/sams

FILES=`ls $(dirname $0)/../front-end/dist/bees-dwh-front-end-*.tar.gz`
NUM=`echo "$FILES" | wc -l`

if [ $NUM -gt 1 ]; then
    echo "Too many *.tar.gz files in dist folder. Use clean build first!"
    exit -1
fi

echo "Uploading file: ${FILES}"

TMP=`ssh -T ${HOST} tempfile`

scp ${FILES} ${HOST}:${TMP}
ssh -T ${HOST} << EOF
    rm -Rf ${REMOTE_DIR}/*
    tar xvzf ${TMP} -C ${REMOTE_DIR}
    rm ${TMP}
EOF
