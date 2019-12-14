#!/bin/bash
set -ev

cd network/

./teardown.sh

cd ../state

rm -rf orderer/*
rm -rf peer/*

echo "All clear."
