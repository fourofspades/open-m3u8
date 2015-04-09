package com.iheartradio.m3u8;

import com.iheartradio.m3u8.data.EncryptionData;
import com.iheartradio.m3u8.data.EncryptionData.Builder;
import com.iheartradio.m3u8.data.EncryptionMethod;
import com.iheartradio.m3u8.data.PlaylistType;
import com.iheartradio.m3u8.data.TrackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

abstract class MediaPlaylistTagHandler extends ExtTagHandler {
    @Override
    public void handle(String line, ParseState state) throws ParseException {
        validateNotMaster(state);
        state.setMedia();
        super.handle(line, state);
    }

    private void validateNotMaster(ParseState state) throws ParseException {
        if (state.isMaster()) {
            throw ParseException.create(ParseExceptionType.MEDIA_IN_MASTER, getTag());
        }
    }

    // media playlist tags
    static final IExtTagHandler EXT_X_ENDLIST = new MediaPlaylistTagHandler() {
        @Override
        public String getTag() {
            return Constants.EXT_X_ENDLIST_TAG;
        }

        @Override
        boolean hasData() {
            return false;
        }

        @Override
        public void handle(String line, ParseState state) throws ParseException {
            super.handle(line, state);
            
            //TODO we should ensure that no new items are added beyond this point to the playlist
        }
    };
    
    static final IExtTagHandler EXT_X_PLAYLIST_TYPE = new MediaPlaylistTagHandler() {
        @Override
        public String getTag() {
            return Constants.EXT_X_PLAYLIST_TYPE_TAG;
        }

        @Override
        boolean hasData() {
            return true;
        }

        @Override
        public void handle(String line, ParseState state) throws ParseException {
            super.handle(line, state);

            final Matcher matcher = match(Constants.EXT_X_PLAYLIST_TYPE_PATTERN, line);

            if (state.getMedia().targetDuration != null) {
                throw ParseException.create(ParseExceptionType.MULTIPLE_EXT_TAG_INSTANCES, getTag(), line);
            }

            state.getMedia().playlistType = ParseUtil.parseEnum(matcher.group(1), PlaylistType.class, getTag());
        }
    };
    
    static final IExtTagHandler EXT_X_TARGETDURATION = new MediaPlaylistTagHandler() {
        @Override
        public String getTag() {
            return Constants.EXT_X_TARGETDURATION_TAG;
        }

        @Override
        boolean hasData() {
            return true;
        }

        @Override
        public void handle(String line, ParseState state) throws ParseException {
            super.handle(line, state);

            final Matcher matcher = match(Constants.EXT_X_TARGETDURATION_PATTERN, line);

            if (state.getMedia().targetDuration != null) {
                throw ParseException.create(ParseExceptionType.MULTIPLE_EXT_TAG_INSTANCES, getTag(), line);
            }

            state.getMedia().targetDuration = ParseUtil.parseInt(matcher.group(1), getTag());
        }
    };

    static final IExtTagHandler EXT_X_MEDIA_SEQUENCE = new MediaPlaylistTagHandler() {
        @Override
        public String getTag() {
            return Constants.EXT_X_MEDIA_SEQUENCE_TAG;
        }

        @Override
        boolean hasData() {
            return true;
        }

        @Override
        public void handle(String line, ParseState state) throws ParseException {
            super.handle(line, state);

            final Matcher matcher = match(Constants.EXT_X_MEDIA_SEQUENCE_PATTERN, line);

            if (state.getMedia().mediaSequenceNumber != null) {
                throw ParseException.create(ParseExceptionType.MULTIPLE_EXT_TAG_INSTANCES, getTag(), line);
            }

            state.getMedia().mediaSequenceNumber = ParseUtil.parseInt(matcher.group(1), getTag());
        }
    };

    static final IExtTagHandler EXT_X_ALLOW_CACHE = new MediaPlaylistTagHandler() {
        @Override
        public String getTag() {
            return Constants.EXT_X_ALLOW_CACHE_TAG;
        }

        @Override
        boolean hasData() {
            return true;
        }

        @Override
        public void handle(String line, ParseState state) throws ParseException {
            super.handle(line, state);

            // deprecated
        }
    };

    // media segment tags

    static final IExtTagHandler EXTINF = new MediaPlaylistTagHandler() {
        @Override
        public String getTag() {
            return Constants.EXTINF_TAG;
        }

        @Override
        boolean hasData() {
            return true;
        }

        @Override
        public void handle(String line, ParseState state) throws ParseException {
            super.handle(line, state);

            final Matcher matcher = match(Constants.EXTINF_PATTERN, line);

            state.getMedia().trackInfo = new TrackInfo(ParseUtil.parseFloat(matcher.group(1), getTag()), matcher.group(2));
        }
    };

    static final IExtTagHandler EXT_X_KEY = new MediaPlaylistTagHandler() {
        private final Map<String, AttributeHandler<EncryptionData.Builder>> HANDLERS = new HashMap<String, AttributeHandler<EncryptionData.Builder>>();
        private final String METHOD = "METHOD";
        private final String URI = "URI";
        private final String IV = "IV";
        private final String KEY_FORMAT = "KEYFORMAT";
        private final String KEY_FORMAT_VERSIONS = "KEYFORMATVERSIONS";

        {
            HANDLERS.put(METHOD, new AttributeHandler<EncryptionData.Builder>() {
                @Override
                public void handle(Attribute attribute, Builder builder, ParseState state) throws ParseException {
                    final EncryptionMethod method = EncryptionMethod.fromValue(attribute.value);

                    if (method == null) {
                        throw ParseException.create(ParseExceptionType.INVALID_ENCRYPTION_METHOD, getTag(), attribute.toString());
                    } else {
                        builder.withMethod(method);
                    }
                }
            });

            HANDLERS.put(URI, new AttributeHandler<EncryptionData.Builder>() {
                @Override
                public void handle(Attribute attribute, Builder builder, ParseState state) throws ParseException {
                    builder.withUri(ParseUtil.decodeUrl(ParseUtil.parseQuotedString(attribute.value, getTag()), state.encoding));
                }
            });

            HANDLERS.put(IV, new AttributeHandler<EncryptionData.Builder>() {
                @Override
                public void handle(Attribute attribute, Builder builder, ParseState state) throws ParseException {
                    final List<Byte> initializationVector = ParseUtil.parseHexadecimal(attribute.value, getTag());

                    if ((initializationVector.size() != Constants.IV_SIZE) && 
                        (initializationVector.size() != Constants.IV_SIZE_ALTERNATIVE)) {
                        throw ParseException.create(ParseExceptionType.INVALID_IV_SIZE, getTag(), attribute.toString());
                    }

                    builder.withInitializationVector(initializationVector);
                }
            });

            HANDLERS.put(KEY_FORMAT, new AttributeHandler<EncryptionData.Builder>() {
                @Override
                public void handle(Attribute attribute, Builder builder, ParseState state) throws ParseException {
                    builder.withKeyFormat(ParseUtil.parseQuotedString(attribute.value, getTag()));
                }
            });

            HANDLERS.put(KEY_FORMAT_VERSIONS, new AttributeHandler<EncryptionData.Builder>() {
                @Override
                public void handle(Attribute attribute, Builder builder, ParseState state) throws ParseException {
                    String[] versionStrings = ParseUtil.parseQuotedString(attribute.value, getTag()).split("/");
                    final List<Integer> versions = new ArrayList<Integer>();

                    for (String version : versionStrings) {
                        try {
                            versions.add(Integer.parseInt(version));
                        } catch (NumberFormatException exception) {
                            throw ParseException.create(ParseExceptionType.INVALID_KEY_FORMAT_VERSIONS, getTag(), attribute.toString());
                        }
                    }

                    builder.withKeyFormatVersions(versions);
                }
            });
        }

        @Override
        public String getTag() {
            return Constants.EXT_X_KEY_TAG;
        }

        @Override
        boolean hasData() {
            return true;
        }

        @Override
        public void handle(String line, ParseState state) throws ParseException {
            super.handle(line, state);

            final EncryptionData.Builder builder = new EncryptionData.Builder()
                    .withKeyFormat(Constants.DEFAULT_KEY_FORMAT)
                    .withKeyFormatVersions(Constants.DEFAULT_KEY_FORMAT_VERSIONS);

            parseAttributes(line, builder, state, HANDLERS);
            final EncryptionData encryptionData = builder.build();

            if (encryptionData.getMethod() != EncryptionMethod.NONE && encryptionData.getUri() == null) {
                throw ParseException.create(ParseExceptionType.MISSING_ENCRYPTION_URI, getTag(), line);
            }

            state.getMedia().encryptionData = encryptionData;
        }
    };
}
