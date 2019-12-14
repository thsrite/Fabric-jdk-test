#!/bin/bash
#
# Copyright IBM Corp All Rights Reserved
#
# SPDX-License-Identifier: Apache-2.0
#

# Exit on first error, print all commands.
set -ev
# delete previous creds
rm -rf ~/.pig-fabric-store/*

# copy peer admin credentials into the keyValStore
mkdir -p ~/.pig-fabric-store
cp creds/* ~/.pig-fabric-store
