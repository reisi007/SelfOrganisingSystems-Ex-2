package at.reisisoft.sos;

import at.reisisoft.sos.directormessage.DirectorInit;
import at.reisisoft.sos.stockmarket.bidder.BuyDownSellUpBidder;
import at.reisisoft.sos.stockmarket.bidder.BuyUpSellDownBidder;
import at.reisisoft.sos.stockmarket.director.StockMarketDirectorAgent;

import java.io.File;

/**
 * Created by Florian on 19.12.2016.
 */
public class StockMain {

    public static void main(String[] args) {
        DirectorInit initMessage = new DirectorInit(
                new File("D:\\Desktop\\sos.csv"),
                1000
        );

        initMessage.addData(BuyDownSellUpBidder.class, 10);
        initMessage.addData(BuyUpSellDownBidder.class, 10);

        MyAkkaMain.startActorSystem(StockMarketDirectorAgent.class, initMessage);
    }
}
