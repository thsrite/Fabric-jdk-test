/*
 *  Copyright 2018 Aliyun.com All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.pig4cloud.pig.fabric.biz.baas;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InfoException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class Main {

    private static final Log logger = LogFactory.getLog(Main.class);
    private static final long waitTime = 6000;
    private static String connectionProfilePath;

    private static String channelName = "first-channel";
    private static String userName = "user";
    private static String secret = "User@1234";
    private static String chaincodeName = "sacc";
    private static String chaincodeVersion = "1.0.0";

    public static void main(String[] args) {

        connectionProfilePath = System.getProperty("user.dir") + "/connection-profile-standard.yaml";
        File f = new File(connectionProfilePath);
        try {

            NetworkConfig networkConfig = NetworkConfig.fromYamlFile(f);
            NetworkConfig.OrgInfo clientOrg = networkConfig.getClientOrganization();
            NetworkConfig.CAInfo caInfo = clientOrg.getCertificateAuthorities().get(0);

            FabricUser user = getFabricUser(clientOrg, caInfo);

            HFClient client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(user);

            Channel channel = client.loadChannelFromConfig(channelName, networkConfig);

            //service discovery function.
            //Peer p = channel.getPeers().iterator().next();
            //channel.removePeer(p);
            //channel.addPeer(p, Channel.PeerOptions.createPeerOptions().addPeerRole(Peer.PeerRole.SERVICE_DISCOVERY));
            //Collection<String> cc = channel.getDiscoveredChaincodeNames();

            channel.initialize();

            channel.registerBlockListener(blockEvent -> {
                logger.info(String.format("Receive block event (number %s) from %s", blockEvent.getBlockNumber(), blockEvent.getPeer()));
            });
            printChannelInfo(client, channel);
            executeChaincode(client, channel);

            logger.info("Shutdown channel.");
            channel.shutdown(true);

        } catch (Exception e) {
            logger.error("exception", e);
        }
    }

    private static void lineBreak() {
        logger.info("=============================================================");
    }

    private static void executeChaincode(HFClient client, Channel channel) throws
            ProposalException, InvalidArgumentException, UnsupportedEncodingException, InterruptedException,
            ExecutionException, TimeoutException
    {
        lineBreak();
        ChaincodeExecuter executer = new ChaincodeExecuter(chaincodeName, chaincodeVersion);

        String newValue = String.valueOf(new Random().nextInt(1000));
        executer.executeTransaction(client, channel, true,"set", "baas", newValue);
        executer.executeTransaction(client, channel, false,"query", "baas");

        lineBreak();
        newValue = String.valueOf(new Random().nextInt(1000));
        executer.executeTransaction(client, channel, true,"set", "baas", newValue);
        executer.executeTransaction(client, channel, false,"query", "baas");

    }
    private static void printChannelInfo(HFClient client, Channel channel) throws
            ProposalException, InvalidArgumentException, IOException
    {
        lineBreak();
        BlockchainInfo channelInfo = channel.queryBlockchainInfo();

        logger.info("Channel height: " + channelInfo.getHeight());
        for (long current = channelInfo.getHeight() - 1; current > -1; --current) {
            BlockInfo returnedBlock = channel.queryBlockByNumber(current);
            final long blockNumber = returnedBlock.getBlockNumber();

            logger.info(String.format("Block #%d has previous hash id: %s", blockNumber, Hex.encodeHexString(returnedBlock.getPreviousHash())));
            logger.info(String.format("Block #%d has data hash: %s", blockNumber, Hex.encodeHexString(returnedBlock.getDataHash())));
            logger.info(String.format("Block #%d has calculated block hash is %s",
                    blockNumber, Hex.encodeHexString(SDKUtils.calculateBlockHash(client,blockNumber, returnedBlock.getPreviousHash(), returnedBlock.getDataHash()))));
        }

    }

    private static FabricUser getFabricUser(NetworkConfig.OrgInfo clientOrg, NetworkConfig.CAInfo caInfo) throws
            MalformedURLException, org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException, InfoException,
            EnrollmentException
    {
        HFCAClient hfcaClient = HFCAClient.createNewInstance(caInfo);
        HFCAInfo cainfo = hfcaClient.info();
        lineBreak();
        logger.info("CA name: " + cainfo.getCAName());
        logger.info("CA version: " + cainfo.getVersion());

        // Persistence is not part of SDK.

        logger.info("Going to enroll user: " + userName);
        Enrollment enrollment = hfcaClient.enroll(userName, secret);
        logger.info("Enroll user: " + userName +  " successfully.");

        FabricUser user = new FabricUser();
        user.setMspId(clientOrg.getMspId());
        user.setName(userName);
        user.setOrganization(clientOrg.getName());
        user.setEnrollment(enrollment);
        return user;
    }
}
