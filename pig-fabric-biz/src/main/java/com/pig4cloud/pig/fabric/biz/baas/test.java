package com.pig4cloud.pig.fabric.biz.baas;
import com.alibaba.fastjson.JSON;
import com.pig4cloud.pig.fabric.biz.baas.vo.testObject;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InfoException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.lang.String.format;

/**
 * Created by box19 on 2019/9/19.
 */
public class test {
    private static final Log logger = LogFactory.getLog(test.class);
    private static String connectionProfilePath;
    private static String channelName = "mychannel";
    private static String userName = "admin";
    private static String secret = "adminpw";

    public static void main(String[] args) throws NetworkConfigurationException, IOException, InvalidArgumentException, InfoException, EnrollmentException, org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, CryptoException, ClassNotFoundException, ProposalException, TransactionException, InterruptedException, ExecutionException, TimeoutException {
        connectionProfilePath = "pig-fabric-biz/src/main/resources/network-config-test.yaml";
        File f = new File(connectionProfilePath);
        NetworkConfig networkConfig = NetworkConfig.fromYamlFile(f);
        NetworkConfig.OrgInfo clientOrg = networkConfig.getClientOrganization();
        NetworkConfig.CAInfo caInfo = clientOrg.getCertificateAuthorities().get(0);

        FabricUser user = getFabricUser(clientOrg, caInfo);

        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        client.setUserContext(user);

        Channel channel = client.loadChannelFromConfig(channelName, networkConfig);
        channel.initialize();

        channel.registerBlockListener(blockEvent -> {
            out(String.format("Receive block event (number %s) from %s", blockEvent.getBlockNumber(), blockEvent.getPeer()));
        });
        printChannelInfo(client, channel);

    }

    private static FabricUser getFabricUser(NetworkConfig.OrgInfo clientOrg, NetworkConfig.CAInfo caInfo) throws
            MalformedURLException, org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException, InfoException,
            EnrollmentException
    {
        HFCAClient hfcaClient = HFCAClient.createNewInstance(caInfo);
        HFCAInfo cainfo = hfcaClient.info();
        out("CA name: " + cainfo.getCAName());
        out("CA version: " + cainfo.getVersion());

        // Persistence is not part of SDK.

        out("Going to enroll user: " + userName);
        Enrollment enrollment = hfcaClient.enroll(userName, secret);
        out("Enroll user: " + userName +  " successfully.");

        FabricUser user = new FabricUser();
        user.setMspId(clientOrg.getMspId());
        user.setName(userName);
        user.setOrganization(clientOrg.getName());
        user.setEnrollment(enrollment);
        return user;
    }
    static void out(String format, Object... args) {

        System.err.flush();
        System.out.flush();
        System.out.println(format(format, args));
        System.err.flush();
        System.out.flush();

    }

    private static void printChannelInfo(HFClient client, Channel channel) throws
            ProposalException, InvalidArgumentException, IOException
    {
        BlockchainInfo channelInfo = channel.queryBlockchainInfo();

        out("Channel height: " + channelInfo.getHeight());
        for (long current = channelInfo.getHeight() - 1; current > -1; --current) {
            BlockInfo returnedBlock = channel.queryBlockByNumber(current);
            final long blockNumber = returnedBlock.getBlockNumber();

            out(String.format("Block #%d has previous hash id: %s", blockNumber, Hex.encodeHexString(returnedBlock.getPreviousHash())));
            out(String.format("Block #%d has data hash: %s", blockNumber, Hex.encodeHexString(returnedBlock.getDataHash())));
            out(String.format("Block #%d has calculated block hash is %s",
                    blockNumber, Hex.encodeHexString(SDKUtils.calculateBlockHash(client,blockNumber, returnedBlock.getPreviousHash(), returnedBlock.getDataHash()))));
        }

    }

    private static void executeChaincode(HFClient client, Channel channel,FabricUser user) throws
            ProposalException, InvalidArgumentException, UnsupportedEncodingException, InterruptedException,
            ExecutionException, TimeoutException
    {
        client.setUserContext(user);
        out("Creating install proposal");
        InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();


    }

    private static void inserttestObject(HFClient client, Channel channel,FabricUser user,String chaincodeid, testObject o) throws
            ProposalException, InvalidArgumentException, UnsupportedEncodingException, InterruptedException,
            ExecutionException, TimeoutException {
        client.setUserContext(user);
        out("Creating install proposal");
        TransactionProposalRequest installProposalRequest = client.newTransactionProposalRequest();
        installProposalRequest.setChaincodeID("");
        installProposalRequest.setFcn("inc");
        installProposalRequest.setArgs(JSON.toJSONString(o));
        Collection<ProposalResponse> rsp2 = channel.sendTransactionProposal(installProposalRequest);
    }

}
