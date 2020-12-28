package org.apache.unomi.utils;

import java.util.stream.Stream;

public class AttributionDefinitionType implements AttributionDefinition {
    public static final String TouchTypePaidAds = "Paid Ads";
    public static final String TouchTypeOrganic = "Organic";
    public static final String TouchTypeDirect = "Direct";
    public static final String TouchTypeOwned = "Owned";
    public static final String TouchTypeUnDetected = "UnDetected";

    private String touch_type;
    private String channel;
    private boolean isMatch;

    private AttributionDefinitionType next;
    private AttributionDefinition sourcing;


    public AttributionDefinitionType Matches(String destinationUrl, String referralUrl) {
        String params = destinationUrl.substring(destinationUrl.indexOf("?") + 1) + "&" + referralUrl.substring(referralUrl.indexOf("?") + 1);
        String utm_medium = Stream.of(params.split("&"))
                .map(kv -> kv.split("="))
                .filter(kv -> "utm_medium".equalsIgnoreCase(kv[0]))
                .map(kv -> kv[1])
                .findFirst()
                .orElse("null");

        String utm_source = Stream.of(params.split("&"))
                .map(kv -> kv.split("="))
                .filter(kv -> "utm_source".equalsIgnoreCase(kv[0]))
                .map(kv -> kv[1])
                .findFirst()
                .orElse("null");
        return this.matches(utm_medium, utm_source, params);
    }

    public String GetTouchType() {
        return touch_type;
    }

    public String GetChannel() {
        return channel;
    }

    public AttributionDefinitionType(String touchType, String channel, boolean isMatch) {
        this.touch_type = touchType;
        this.channel = channel;
        this.isMatch = isMatch;
    }

    public AttributionDefinitionType(AttributionDefinition sourcing) {
        this.sourcing = sourcing;
    }

    public static AttributionDefinitionType Create() {
        AttributionDefinitionType attributionDefinitionType = new AttributionDefinitionType(new DefaultAttribution());
        attributionDefinitionType.setNext(new GoogleAttribution()).setNext(new FacebookAttribution()).
                setNext(new InstagramAttribution()).
                setNext(new BingAttribution()).
                setNext(new SnapchatAttribution()).
                setNext(new PinterestAttribution()).
                setNext(new TwitterAttribution()).
                setNext(new TiktokAttribution()).
                setNext(new OtherAdsAttribution()).
                setNext(new AdrollAttribution()).
                setNext(new DisplayAttribution()).
                setNext(new VideoAttribution()).
                setNext(new AffiliatesAttribution()).
                setNext(new AmazonAttribution()).
                setNext(new ReferralAttribution()).
                setNext(new EmailAttribution()).
                setNext(new MailchimpAttribution()).
                setNext(new SendgridAttribution()).
                setNext(new WebPushAttribution()).
                setNext(new MobilePushAttribution()).
                setNext(new OnSiteAttribution());
        return attributionDefinitionType;
    }

    public AttributionDefinitionType setNext(AttributionDefinition sourcing) {
        if (this.next != null) {
            return this.next.setNext(sourcing);
        }
        this.next = new AttributionDefinitionType(sourcing);
        return this;
    }

    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        AttributionDefinitionType attributionDefinitionType =  this.sourcing.matches(utm_medium, utm_source, params);
        if (attributionDefinitionType.isMatch ) {
            return attributionDefinitionType;
        }
        if (this.next != null) {
            return this.next.matches(utm_medium,utm_source,params);
        }
        return attributionDefinitionType;
    }
}

interface AttributionDefinition {
    AttributionDefinitionType matches(String utm_medium, String utm_source, String params);
}

class DefaultAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class GoogleAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_source.toLowerCase().matches("^(?!.*(msn|yahoo|gemini|facebook|instagram|pinterest|twitter|tiktok|snapchat)).*$") &&
                utm_medium.toLowerCase().matches("^.*(cpc|ppc|paidsearch).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypePaidAds, "google", true);
        }

        if ((utm_medium.toLowerCase().matches("^.*(organic).*$") ||
                utm_source.toLowerCase().matches("^.*(organic).*$")) && params.contains("gclid")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOrganic, "google", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class FacebookAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_source.toLowerCase().matches("^.*(facebook|fb).*$") &&
                utm_medium.toLowerCase().matches("^.*(cpc|ppc|paid_social|paidsocial|paid social).*$") &&
                params.contains("fbclid")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypePaidAds, "facebook", true);
        }

        if (utm_medium.toLowerCase().matches("^.*(social|social-network|social-media|sm|social network|social media|referral).*$") &&
                utm_source.toLowerCase().matches("^.*(facebook).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOrganic, "facebook", true);
        }

        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class BingAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(cpc|ppc|paidsearch).*$") &&
                utm_source.toLowerCase().matches("^.*(bing|msn|yahoo|gemini).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypePaidAds, "bing", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class InstagramAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(cpc|ppc|paid_social|paidsocial|paid social|referral).*$") &&
                utm_source.toLowerCase().matches("^.*(instagram|igshopping).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypePaidAds, "instagram", true);
        }

        if (utm_medium.toLowerCase().matches("^.*(social|social-network|social-media|sm|social network|social media|referral).*$") &&
                utm_source.toLowerCase().matches("^.*(instagram).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOrganic, "instagram", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class PinterestAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(cpc|ppc|paid_social|paidsocial|paid-social|paid social).*$") &&
                utm_source.toLowerCase().matches("^.*(pinterest).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypePaidAds, "pinterest", true);

        }
        if (utm_medium.toLowerCase().matches("^.*(social|social-network|social-media|sm|social network|social media|referral).*$") &&
                utm_source.toLowerCase().matches("^.*(pinterest).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOrganic, "pinterest", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class SnapchatAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().toLowerCase().matches("^.*(cpc|ppc|paid_social|paidsocial|paid-social|paid scocial).*$") &&
                utm_source.toLowerCase().matches("^.*(snapchat).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypePaidAds, "snapchat", true);

        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class TwitterAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(cpc|ppc|paid_social|paidsocial|paid-social|paid scocial).*$") &&
                utm_source.toLowerCase().matches("^.*(twitter).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypePaidAds, "twitter", true);
        }
        if (utm_medium.toLowerCase().matches("^.*(social|social-network|social-media|sm|social network|social media|referral).*$") &&
                utm_source.toLowerCase().matches("^.*(twitter).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOrganic, "twitter", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class TiktokAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(cpc|ppc|paid_social|paidsocial|paid-social|paid scocial).*$") &&
                utm_source.toLowerCase().matches("^.*(tiktok).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypePaidAds, "tiktok", true);
        }
        if (utm_medium.toLowerCase().matches("^.*(social|social-network|social-media|sm|social network|social media|referral).*$") &&
                utm_source.toLowerCase().matches("^.*(tiktok).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOrganic, "tiktok", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class OtherAdsAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(cpv|cpa|cpp|content-text).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypePaidAds, "other advertising", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class AdrollAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_source.toLowerCase().matches("^.*(adroll).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypePaidAds, "adroll", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class DisplayAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(display|cpm|banner).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypePaidAds, "display", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class VideoAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(video).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypePaidAds, "video", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class AffiliatesAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(affiliate|affiliates).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypePaidAds, "affiliates", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class AmazonAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(referral).*$") && utm_source.toLowerCase().matches("^.*(amazon).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOrganic, "amazon", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class ReferralAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(referral).*$") && utm_source.toLowerCase().matches("^(?!.*(facebook|instagram|pinterest|twitter|tiktok|amazon)).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOrganic, "referral", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class EmailAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(email|e-mail).*$") && utm_source.toLowerCase().matches("^.*(email).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOwned, "email", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class MailchimpAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(email|e-mail).*$") && utm_source.toLowerCase().matches("^.*(mailchimp).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOwned, "mailchimp", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class SendgridAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(email|e-mail).*$") && utm_source.toLowerCase().matches("^.*(sendgrid).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOwned, "sendgrid", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class WebPushAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(webpush|webnoti|webnotifcations).*$") && utm_source.toLowerCase().matches("^.*(webpush|webnoti|webnotifcations|onesignal).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOwned, "web push", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class MobilePushAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(push|mobilepush).*$") && utm_source.toLowerCase().matches("^.*(push|mobilepush|onesignal).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOwned, "mobile push", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

class OnSiteAttribution implements AttributionDefinition {
    @Override
    public AttributionDefinitionType matches(String utm_medium, String utm_source, String params) {
        if (utm_medium.toLowerCase().matches("^.*(onsite|on-site|weblayer|web).*$") && utm_source.toLowerCase().matches("^.*(onsite|on-site|optimonk).*$")) {
            return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeOwned, "on-site", true);
        }
        return new AttributionDefinitionType(AttributionDefinitionType.TouchTypeUnDetected, "", false);
    }
}

