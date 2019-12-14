package com.pig4cloud.pig.fabric.biz.baas;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

import static com.pig4cloud.pig.fabric.biz.baas.ConnectUtil.getConnect;


public class App {

    private static BlockInfo blockInfo;

    public static void main(String[] args) throws Exception{
        System.out.println("counter app");

        String certFilepath = "/root/go/src/github.com/hyperledger/agriculture/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/admincerts/";
        String keyFilepath = "/root/go/src/github.com/hyperledger/agriculture/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore/";
        String localpath = "d:/";
        String hostName = "localhost";
        int port = 22;
        String username = "root";
        String password = "root";

        Connection ss = getConnect(hostName, username, password, port);
        SCPClient scpClient = ss.createSCPClient();

        Session certFiless = ss.openSession();
        certFiless.execCommand("ls ".concat(certFilepath));

        InputStream certFilessis = new StreamGobbler(certFiless.getStdout());
        BufferedReader certFilessbrs = new BufferedReader(new InputStreamReader(certFilessis));

        String certfilename = certFilessbrs.readLine();
        String certfilepath = certFilepath + certfilename;
        scpClient.get(certfilepath,localpath);

        Session keyFiless = ss.openSession();
        keyFiless.execCommand("ls ".concat(keyFilepath));

        InputStream keyFilessis = new StreamGobbler(keyFiless.getStdout());
        BufferedReader keyFilessbrs = new BufferedReader(new InputStreamReader(keyFilessis));
        String keyfilename = keyFilessbrs.readLine();
        String keyfilepath = keyFilepath + keyfilename;
        scpClient.get(keyfilepath,localpath);



        //创建User实例
        //keyFile一般在./crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/keystore
        //cerFile一般在./crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp/signcerts
        //此处的user是admin，也可以是其他用户，不过admin可以管理链码，如实例化和安装链码等，其他用户只能调用链码
        //创建HFClient实例
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        LocalUser user=new LocalUser("Admin","Org1MSP",localpath+keyfilename,localpath+certfilename);//MSPID需要到配置文件中查询
        client.setUserContext(user);

        //创建通道实例
        Channel channel = client.newChannel("mychannel");//channe名必须对应
        Peer peer1 = client.newPeer("peer0.Org1MSP","grpc://192.168.8.97:7051");
        //Peer peer2 = client.newPeer("peer1.Org1MSP","grpc://0.0.0.0:7056");
        channel.addPeer(peer1);
        //channel.addPeer(peer2);
        Orderer orderer = client.newOrderer("orderer.example.com","grpc://192.168.8.97:7050");
        channel.addOrderer(orderer);
        channel.initialize();

        Channel foo = client.getChannel("mychannel");
        BlockchainInfo blockchainInfo;
        blockchainInfo = foo.queryBlockchainInfo();
        long a = blockchainInfo.getHeight();
        System.out.println(a);

        Collection<Peer> peer5 = channel.getPeers();
        channel.queryBlockchainInfo();

        String name;
        name = channel.getName();
        System.out.println(name);

        long a=2;

        blockInfo = channel.queryBlockByNumber(peer1,a);

        byte [] k=new byte[1000];
        k=blockInfo.getDataHash();
        System.out.println(k);

        QueryByChaincodeRequest req = client.newQueryProposalRequest();
        ChaincodeID cid = ChaincodeID.newBuilder().setName("counter-cc").build();
        req.setChaincodeID(cid);
        req.setFcn("value");
        Collection<ProposalResponse> rspc = channel.queryByChaincode(req);
        for(ProposalResponse rsp: rspc){
            System.out.format("status: %d\n",rsp.getStatus().getStatus());
            System.out.format("payload: %s\n", rsp.getProposalResponse().getResponse().getPayload().toStringUtf8());
        }
        ProposalResponse[] rsp = channel.queryByChaincode(req).toArray(new ProposalResponse[0]);
        System.out.format("rsp message => %s\n",rspc[0].getProposalResponse().getResponse().getPayload().toStringUtf8());


        TransactionProposalRequest req2 = client.newTransactionProposalRequest();
        req2.setChaincodeID(cid);
        req2.setFcn("inc");
        req2.setArgs("10");
        Collection<ProposalResponse> rsp2 = channel.sendTransactionProposal(req2);
        TransactionEvent event = channel.sendTransaction(rsp2).get();
        System.out.format("txid: %s\n", event.getTransactionID());
        System.out.format("valid: %b\n", event.isValid());




    }
}
