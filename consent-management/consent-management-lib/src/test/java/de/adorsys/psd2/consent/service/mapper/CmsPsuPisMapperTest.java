/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.pis.CmsBulkPayment;
import de.adorsys.psd2.consent.api.pis.CmsFrequencyCode;
import de.adorsys.psd2.consent.api.pis.CmsPeriodicPayment;
import de.adorsys.psd2.consent.api.pis.CmsSinglePayment;
import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CmsPsuPisMapperTest {
    private static final String PAYMENT_ID = "payment id";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";

    @InjectMocks
    private CmsPsuPisMapper cmsPsuPisMapper;
    @Mock
    private PisCommonPaymentMapper pisCommonPaymentMapper;
    @Mock
    private TppInfoMapper tppInfoMapper;
    @Mock
    private PsuDataMapper psuDataMapper;

    @Test
    public void mapToCmsPayment_SinglePayment_Success() {
        // Given
        List<PisPaymentData> singlePaymentData = buildPisPaymentDataList(PaymentType.SINGLE);

        when(pisCommonPaymentMapper.mapToCmsAddress(any()))
            .thenReturn(null);
        when(tppInfoMapper.mapToTppInfo(any()))
            .thenReturn(new TppInfo());
        when(psuDataMapper.mapToPsuIdDataList(any()))
            .thenReturn(Collections.singletonList(buildPsuIdData()));

        // When
        CmsSinglePayment actualResult = (CmsSinglePayment) cmsPsuPisMapper.mapToCmsPayment(singlePaymentData);

        // Then
        assertNotNull(actualResult);
        assertEquals(TransactionStatus.RCVD, actualResult.getPaymentStatus());
    }

    @Test
    public void mapToCmsPayment_PeriodicPayment_Success() {
        // Given
        List<PisPaymentData> periodicPaymentData = buildPisPaymentDataList(PaymentType.PERIODIC);

        when(pisCommonPaymentMapper.mapToCmsAddress(any()))
            .thenReturn(null);
        when(tppInfoMapper.mapToTppInfo(any()))
            .thenReturn(new TppInfo());
        when(psuDataMapper.mapToPsuIdDataList(any()))
            .thenReturn(Collections.singletonList(buildPsuIdData()));

        // When
        CmsPeriodicPayment actualResult = (CmsPeriodicPayment) cmsPsuPisMapper.mapToCmsPayment(periodicPaymentData);

        // Then
        assertNotNull(actualResult);
        assertEquals(TransactionStatus.RCVD, actualResult.getPaymentStatus());
    }

    @Test
    public void mapToCmsPayment_BulkPayment_Success() {
        // Given
        List<PisPaymentData> bulkPaymentData = buildPisPaymentDataList(PaymentType.BULK);

        when(pisCommonPaymentMapper.mapToCmsAddress(any()))
            .thenReturn(null);
        when(tppInfoMapper.mapToTppInfo(any()))
            .thenReturn(new TppInfo());
        when(psuDataMapper.mapToPsuIdDataList(any()))
            .thenReturn(Collections.singletonList(buildPsuIdData()));

        // When
        CmsBulkPayment actualResult = (CmsBulkPayment) cmsPsuPisMapper.mapToCmsPayment(bulkPaymentData);

        // Then
        assertNotNull(actualResult);
        assertTrue(CollectionUtils.isNotEmpty(actualResult.getPayments()));
    }

    private List<PisPaymentData> buildPisPaymentDataList(PaymentType paymentType) {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentId(PAYMENT_ID);
        pisPaymentData.setPaymentData(buildPisCommonPaymentData(paymentType));
        pisPaymentData.setDebtorAccount(buildAccountReference());
        pisPaymentData.setCreditorAccount(buildAccountReference());
        pisPaymentData.setAmount(new BigDecimal("1000"));
        pisPaymentData.setCurrency(Currency.getInstance("EUR"));
        pisPaymentData.setFrequency(CmsFrequencyCode.DAILY.name());

        return Collections.singletonList(pisPaymentData);
    }

    private PisCommonPaymentData buildPisCommonPaymentData(PaymentType paymentType) {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(TransactionStatus.RCVD);
        pisCommonPaymentData.setPsuDataList(Collections.singletonList(buildPsuData()));
        pisCommonPaymentData.setPaymentType(paymentType);
        pisCommonPaymentData.setPaymentProduct(PAYMENT_PRODUCT);
        pisCommonPaymentData.setPayments(buildPisPaymentDataListForCommonData());
        pisCommonPaymentData.setTppInfo(buildTppInfo());
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC));
        return pisCommonPaymentData;
    }

    private AccountReferenceEntity buildAccountReference() {
        AccountReferenceEntity pisAccountReference = new AccountReferenceEntity();
        pisAccountReference.setIban("iban");
        pisAccountReference.setCurrency(Currency.getInstance("EUR"));

        return pisAccountReference;
    }

    private TppInfoEntity buildTppInfo() {
        TppInfoEntity tppInfoEntity = new TppInfoEntity();
        tppInfoEntity.setNokRedirectUri("tpp nok redirect uri");
        tppInfoEntity.setRedirectUri("tpp ok redirect uri");

        return tppInfoEntity;
    }

    private List<PisPaymentData> buildPisPaymentDataListForCommonData() {
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentId(PAYMENT_ID);
        pisPaymentData.setAmount(new BigDecimal("1000"));
        pisPaymentData.setCurrency(Currency.getInstance("EUR"));

        return Collections.singletonList(pisPaymentData);
    }

    private PsuData buildPsuData() {
        PsuIdData psuIdData = buildPsuIdData();
        PsuData psuData = new PsuData(
            psuIdData.getPsuId(),
            psuIdData.getPsuIdType(),
            psuIdData.getPsuCorporateId(),
            psuIdData.getPsuCorporateIdType()
        );
        psuData.setId(1L);

        return psuData;
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(
            "psuId",
            "psuIdType",
            "psuCorporateId",
            "psuCorporateIdType"
        );
    }

}
