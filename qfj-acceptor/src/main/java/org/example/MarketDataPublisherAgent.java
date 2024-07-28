package org.example;

import org.agrona.concurrent.Agent;
import org.agrona.concurrent.SystemEpochClock;
import quickfix.Session;
import quickfix.field.*;
import quickfix.fix44.MarketDataIncrementalRefresh;

public class MarketDataPublisherAgent implements Agent {

    private final AcceptorApplication application;

    private int maxMessagePerWindow = 1;

    private int messageInThisWindow = 0;

    private long windowStartTime = SystemEpochClock.INSTANCE.time();

    private long windowLengthInMs = 1_000;



    public MarketDataPublisherAgent(AcceptorApplication application) {
        this.application = application;
    }

    @Override
    public void onStart() {
        Agent.super.onStart();
    }

    /**
     * <message name="MarketDataIncrementalRefresh" msgtype="X" msgcat="app">
     *             <field name="MDReqID" required="N"/>
     *             <group name="NoMDEntries" required="Y">
     *                 <field name="MDUpdateAction" required="Y"/>
     *                 <field name="DeleteReason" required="N"/>
     *                 <field name="MDEntryType" required="N"/>
     *                 <field name="MDEntryID" required="N"/>
     *                 <field name="MDEntryRefID" required="N"/>
     *                 <component name="Instrument" required="N"/>
     *                 <group name="NoUnderlyings" required="N">
     *                     <component name="UnderlyingInstrument" required="N"/>
     *                 </group>
     *                 <group name="NoLegs" required="N">
     *                     <component name="InstrumentLeg" required="N"/>
     *                 </group>
     *                 <field name="FinancialStatus" required="N"/>
     *                 <field name="CorporateAction" required="N"/>
     *                 <field name="MDEntryPx" required="N"/>
     *                 <field name="Currency" required="N"/>
     *                 <field name="MDEntrySize" required="N"/>
     *                 <field name="MDEntryDate" required="N"/>
     *                 <field name="MDEntryTime" required="N"/>
     *                 <field name="TickDirection" required="N"/>
     *                 <field name="MDMkt" required="N"/>
     *                 <field name="TradingSessionID" required="N"/>
     *                 <field name="TradingSessionSubID" required="N"/>
     *                 <field name="QuoteCondition" required="N"/>
     *                 <field name="TradeCondition" required="N"/>
     *                 <field name="MDEntryOriginator" required="N"/>
     *                 <field name="LocationID" required="N"/>
     *                 <field name="DeskID" required="N"/>
     *                 <field name="OpenCloseSettlFlag" required="N"/>
     *                 <field name="TimeInForce" required="N"/>
     *                 <field name="ExpireDate" required="N"/>
     *                 <field name="ExpireTime" required="N"/>
     *                 <field name="MinQty" required="N"/>
     *                 <field name="ExecInst" required="N"/>
     *                 <field name="SellerDays" required="N"/>
     *                 <field name="OrderID" required="N"/>
     *                 <field name="QuoteEntryID" required="N"/>
     *                 <field name="MDEntryBuyer" required="N"/>
     *                 <field name="MDEntrySeller" required="N"/>
     *                 <field name="NumberOfOrders" required="N"/>
     *                 <field name="MDEntryPositionNo" required="N"/>
     *                 <field name="Scope" required="N"/>
     *                 <field name="PriceDelta" required="N"/>
     *                 <field name="NetChgPrevDay" required="N"/>
     *                 <field name="Text" required="N"/>
     *                 <field name="EncodedTextLen" required="N"/>
     *                 <field name="EncodedText" required="N"/>
     *             </group>
     *             <field name="ApplQueueDepth" required="N"/>
     *             <field name="ApplQueueResolution" required="N"/>
     *         </message>
     * */
    @Override
    public int doWork() throws Exception {
        if (application.isHasLogOn()) {
            long timeNow = SystemEpochClock.INSTANCE.time();
            if (timeNow > windowStartTime + windowLengthInMs) {
                windowStartTime = timeNow;
                messageInThisWindow = 0;
            }

            if (messageInThisWindow > maxMessagePerWindow) {
                MarketDataIncrementalRefresh message = new MarketDataIncrementalRefresh();
                MarketDataIncrementalRefresh.NoMDEntries group = new MarketDataIncrementalRefresh.NoMDEntries();
                group.set(new MDUpdateAction(MDUpdateAction.NEW));
                group.set(new MDEntrySize(1000));
                group.set(new MDEntryPx(12.97));
                group.set(new MDEntryID("BNA"));
                message.addGroup(group);
                Session.sendToTarget(message, application.sessionID());

                messageInThisWindow++;
            }
        }
        return 0;
    }

    @Override
    public void onClose() {
        Agent.super.onClose();
    }

    @Override
    public String roleName() {
        return "publisher agent";
    }
}
