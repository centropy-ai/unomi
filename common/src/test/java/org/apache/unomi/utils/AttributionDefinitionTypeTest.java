package org.apache.unomi.utils;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class AttributionDefinitionTypeTest extends TestCase {
    @Test
    public void testAttributionTypeIsPaid() {
        AttributionDefinitionType attributionGoogle = AttributionDefinitionType.Create();
        attributionGoogle = attributionGoogle.Matches("http://app.primedata.ai?utm_source=google", "http://google.com?utm_medium=cpc");
        Assert.assertEquals("touch type is paid for google", AttributionDefinitionType.TouchTypePaidAds, attributionGoogle.GetTouchType());
        Assert.assertEquals("channel is google", "google", attributionGoogle.GetChannel());

        AttributionDefinitionType attributionFacebook = AttributionDefinitionType.Create();
        attributionFacebook = attributionFacebook.Matches("http://app.primedata.ai?ptm=1", "http://facebook.com?utm_medium=paid_social&utm_source=facebook&fbclid=123");
        Assert.assertEquals("touch type is paid for facebook", AttributionDefinitionType.TouchTypePaidAds, attributionFacebook.GetTouchType());
        Assert.assertEquals("channel is facebook", "facebook", attributionFacebook.GetChannel());

        AttributionDefinitionType attributionBing = AttributionDefinitionType.Create();
        attributionBing = attributionBing.Matches("http://app.primedata.ai?ptm=1", "http://bing.com?utm_medium=paidsearch&utm_source=yahoo");
        Assert.assertEquals("touch type is paid for bing", AttributionDefinitionType.TouchTypePaidAds, attributionBing.GetTouchType());
        Assert.assertEquals("channel is bing", "bing", attributionBing.GetChannel());

        AttributionDefinitionType attributionInstagram = AttributionDefinitionType.Create();
        attributionInstagram = attributionInstagram.Matches("http://app.primedata.ai?ptm=1", "http://instagram.com?utm_medium=paid social&utm_source=igshopping");
        Assert.assertEquals("touch type is paid for instagram", AttributionDefinitionType.TouchTypePaidAds, attributionInstagram.GetTouchType());
        Assert.assertEquals("channel is instagram", "instagram", attributionInstagram.GetChannel());

        AttributionDefinitionType attributionPinterest = AttributionDefinitionType.Create();
        attributionPinterest = attributionPinterest.Matches("http://app.primedata.ai?ptm=1", "http://pinterest.com?utm_medium=paid-social&utm_source=pinterest");
        Assert.assertEquals("touch type is paid for pinterest", AttributionDefinitionType.TouchTypePaidAds, attributionPinterest.GetTouchType());
        Assert.assertEquals("channel is pinterest", "pinterest", attributionPinterest.GetChannel());

        AttributionDefinitionType attributionSnapchat = AttributionDefinitionType.Create();
        attributionSnapchat = attributionSnapchat.Matches("http://app.primedata.ai?ptm=1", "http://snapchat.com?utm_medium=paid-social&utm_source=snapchat");
        Assert.assertEquals("touch type is paid for snapchat", AttributionDefinitionType.TouchTypePaidAds, attributionSnapchat.GetTouchType());
        Assert.assertEquals("channel is snapchat", "snapchat", attributionSnapchat.GetChannel());

        AttributionDefinitionType attributionTwitter = AttributionDefinitionType.Create();
        attributionTwitter = attributionTwitter.Matches("http://app.primedata.ai?ptm=1", "http://twitter.com?utm_medium=paid-social&utm_source=twitter");
        Assert.assertEquals("touch type is paid for twitter", AttributionDefinitionType.TouchTypePaidAds, attributionTwitter.GetTouchType());
        Assert.assertEquals("channel is twitter", "twitter", attributionTwitter.GetChannel());

        AttributionDefinitionType attributionTiktok = AttributionDefinitionType.Create();
        attributionTiktok = attributionTiktok.Matches("http://app.primedata.ai?ptm=1", "http://tikok.com?utm_medium=paid_social&utm_source=tiktok");
        Assert.assertEquals("touch type is paid for tiktok", AttributionDefinitionType.TouchTypePaidAds, attributionTiktok.GetTouchType());
        Assert.assertEquals("channel is tiktok", "tiktok", attributionTiktok.GetChannel());

        AttributionDefinitionType attributionOtherAds = AttributionDefinitionType.Create();
        attributionOtherAds = attributionOtherAds.Matches("http://app.primedata.ai?ptm=1", "http://vnexpress.net?utm_medium=content-text");
        Assert.assertEquals("touch type is paid for other advertising", AttributionDefinitionType.TouchTypePaidAds, attributionOtherAds.GetTouchType());
        Assert.assertEquals("channel is other advertising", "other advertising", attributionOtherAds.GetChannel());

        AttributionDefinitionType attributionAdroll = AttributionDefinitionType.Create();
        attributionAdroll = attributionAdroll.Matches("http://app.primedata.ai?ptm=1", "http://adroll.com?utm_source=adroll");
        Assert.assertEquals("touch type is paid for adroll", AttributionDefinitionType.TouchTypePaidAds, attributionAdroll.GetTouchType());
        Assert.assertEquals("channel is adroll", "adroll", attributionAdroll.GetChannel());

        AttributionDefinitionType attributionDisplay = AttributionDefinitionType.Create();
        attributionDisplay = attributionDisplay.Matches("http://app.primedata.ai?ptm=1", "http://news.zing.vn?utm_medium=banner");
        Assert.assertEquals("touch type is paid for display", AttributionDefinitionType.TouchTypePaidAds, attributionDisplay.GetTouchType());
        Assert.assertEquals("channel is display", "display", attributionDisplay.GetChannel());

        AttributionDefinitionType attributionVideo = AttributionDefinitionType.Create();
        attributionVideo = attributionVideo.Matches("http://app.primedata.ai?ptm=1", "http://youtube.com?utm_medium=video");
        Assert.assertEquals("touch type is paid for video", AttributionDefinitionType.TouchTypePaidAds, attributionVideo.GetTouchType());
        Assert.assertEquals("channel is video", "video", attributionVideo.GetChannel());

        AttributionDefinitionType attributionAffiliates = AttributionDefinitionType.Create();
        attributionAffiliates = attributionAffiliates.Matches("http://app.primedata.ai?ptm=1", "http://youtube.com?utm_medium=affiliates");
        Assert.assertEquals("touch type is paid for affiliates", AttributionDefinitionType.TouchTypePaidAds, attributionAffiliates.GetTouchType());
        Assert.assertEquals("channel is affiliates", "affiliates", attributionAffiliates.GetChannel());
    }


    @Test
    public void testAttributionTypeIsOrganic() {
        AttributionDefinitionType attributionGoogle = AttributionDefinitionType.Create();
        attributionGoogle = attributionGoogle.Matches("http://app.primedata.ai?ptm=1", "http://google.com?utm_medium=organic&gclid=123");
        Assert.assertEquals("touch type is organic for google", AttributionDefinitionType.TouchTypeOrganic, attributionGoogle.GetTouchType());
        Assert.assertEquals("channel is google", "google", attributionGoogle.GetChannel());

        AttributionDefinitionType attributionFacebook = AttributionDefinitionType.Create();
        attributionFacebook = attributionFacebook.Matches("http://app.primedata.ai?ptm=1", "http://facebook.com?utm_medium=sm&utm_source=facebook");
        Assert.assertEquals("touch type is organic for facebook", AttributionDefinitionType.TouchTypeOrganic, attributionFacebook.GetTouchType());
        Assert.assertEquals("channel is facebook", "facebook", attributionFacebook.GetChannel());

        AttributionDefinitionType attributionInstagram = AttributionDefinitionType.Create();
        attributionInstagram = attributionInstagram.Matches("http://app.primedata.ai?ptm=1", "http://instagram.com?utm_medium=social-media&utm_source=instagram");
        Assert.assertEquals("touch type is organic for instagram", AttributionDefinitionType.TouchTypeOrganic, attributionInstagram.GetTouchType());
        Assert.assertEquals("channel is instagram", "instagram", attributionInstagram.GetChannel());

        AttributionDefinitionType attributionPinterest = AttributionDefinitionType.Create();
        attributionPinterest = attributionPinterest.Matches("http://app.primedata.ai?ptm=1", "http://pinterest.com?utm_medium=social-network&utm_source=pinterest");
        Assert.assertEquals("touch type is organic for pinterest", AttributionDefinitionType.TouchTypeOrganic, attributionPinterest.GetTouchType());
        Assert.assertEquals("channel is pinterest", "pinterest", attributionPinterest.GetChannel());

        AttributionDefinitionType attributionTwitter = AttributionDefinitionType.Create();
        attributionTwitter = attributionTwitter.Matches("http://app.primedata.ai?ptm=1", "http://twitter.com?utm_medium=social network&utm_source=twitter");
        Assert.assertEquals("touch type is organic for twitter", AttributionDefinitionType.TouchTypeOrganic, attributionTwitter.GetTouchType());
        Assert.assertEquals("channel is twitter", "twitter", attributionTwitter.GetChannel());

        AttributionDefinitionType attributionTiktok = AttributionDefinitionType.Create();
        attributionTiktok = attributionTiktok.Matches("http://app.primedata.ai?ptm=1", "http://tikok.com?utm_medium=referral&utm_source=tiktok");
        Assert.assertEquals("touch type is organic for tiktok", AttributionDefinitionType.TouchTypeOrganic, attributionTiktok.GetTouchType());
        Assert.assertEquals("channel is tiktok", "tiktok", attributionTiktok.GetChannel());

        AttributionDefinitionType attributionAmazon = AttributionDefinitionType.Create();
        attributionAmazon = attributionAmazon.Matches("http://app.primedata.ai?ptm=1", "http://amazon.com?utm_medium=referral&utm_source=amazon");
        Assert.assertEquals("touch type is organic for other amazon", AttributionDefinitionType.TouchTypeOrganic, attributionAmazon.GetTouchType());
        Assert.assertEquals("channel is other amazon", "amazon", attributionAmazon.GetChannel());

        AttributionDefinitionType attributionReferral = AttributionDefinitionType.Create();
        attributionReferral = attributionReferral.Matches("http://app.primedata.ai?ptm=1", "http://youtube.com?utm_medium=referral&utm_source=vng");
        Assert.assertEquals("touch type is organic for referral", AttributionDefinitionType.TouchTypeOrganic, attributionReferral.GetTouchType());
        Assert.assertEquals("channel is  referral", "referral", attributionReferral.GetChannel());
    }

    @Test
    public void testAttributionTypeIsOwned() {
        AttributionDefinitionType attributionEmail = AttributionDefinitionType.Create();
        attributionEmail = attributionEmail.Matches("http://app.primedata.ai?ptm=1", "http://google.com?utm_medium=email&utm_source=email");
        Assert.assertEquals("touch type is owned for email", AttributionDefinitionType.TouchTypeOwned, attributionEmail.GetTouchType());
        Assert.assertEquals("channel is email", "email", attributionEmail.GetChannel());

        AttributionDefinitionType attributionMailChimp = AttributionDefinitionType.Create();
        attributionMailChimp = attributionMailChimp.Matches("http://app.primedata.ai?ptm=1", "http://google.com?utm_medium=e-mail&utm_source=mailchimp");
        Assert.assertEquals("touch type is owned for mailchimp", AttributionDefinitionType.TouchTypeOwned, attributionMailChimp.GetTouchType());
        Assert.assertEquals("channel is mailchimp", "mailchimp", attributionMailChimp.GetChannel());

        AttributionDefinitionType attributionSendgrid= AttributionDefinitionType.Create();
        attributionSendgrid = attributionSendgrid.Matches("http://app.primedata.ai?ptm=1", "http://google.com?utm_medium=e-mail&utm_source=sendgrid");
        Assert.assertEquals("touch type is owned for sendgrid", AttributionDefinitionType.TouchTypeOwned, attributionSendgrid.GetTouchType());
        Assert.assertEquals("channel is sendgrid", "sendgrid", attributionSendgrid.GetChannel());

        AttributionDefinitionType attributionMobilePush = AttributionDefinitionType.Create();
        attributionMobilePush = attributionMobilePush.Matches("http://app.primedata.ai?ptm=1", "http://google.com?utm_medium=push&utm_source=mobilepush");
        Assert.assertEquals("touch type is owned for mobile push", AttributionDefinitionType.TouchTypeOwned, attributionMobilePush.GetTouchType());
        Assert.assertEquals("channel is mobile push", "mobile push", attributionMobilePush.GetChannel());

        AttributionDefinitionType attributionWebPush = AttributionDefinitionType.Create();
        attributionWebPush = attributionWebPush.Matches("http://app.primedata.ai?ptm=1", "http://facebook.com?utm_medium=webnoti&utm_source=webnotifcations");
        Assert.assertEquals("touch type is owned for web push", AttributionDefinitionType.TouchTypeOwned, attributionWebPush.GetTouchType());
        Assert.assertEquals("channel is web push", "web push", attributionWebPush.GetChannel());

        AttributionDefinitionType attributionOnsite = AttributionDefinitionType.Create();
        attributionOnsite = attributionOnsite.Matches("http://app.primedata.ai?ptm=1", "http://facebook.com?utm_medium=weblayer&utm_source=optimonk");
        Assert.assertEquals("touch type is owned for on-site", AttributionDefinitionType.TouchTypeOwned, attributionOnsite.GetTouchType());
        Assert.assertEquals("channel is on-site", "on-site", attributionOnsite.GetChannel());
    }

    @Test
    public void testAttributionTypeIsUndetected() {
        AttributionDefinitionType attributionEmail = AttributionDefinitionType.Create();
        attributionEmail = attributionEmail.Matches("http://app.primedata.ai?ptm=1", "http://google.com?utm_medium=paidvnexpress&utm_source=vnexpress");
        Assert.assertEquals("touch type is undetected", AttributionDefinitionType.TouchTypeUnDetected, attributionEmail.GetTouchType());
    }
}