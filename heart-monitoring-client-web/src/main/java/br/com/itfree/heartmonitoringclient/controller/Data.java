package br.com.itfree.heartmonitoringclient.controller;

import java.time.Instant;

@lombok.Data
public class Data {

    private final long seqNo;
    private final Instant timestamp;

}
