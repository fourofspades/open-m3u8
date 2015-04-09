package com.iheartradio.m3u8;

import java.util.HashMap;
import java.util.Map;

import com.iheartradio.m3u8.data.MediaData;
import com.iheartradio.m3u8.data.PlaylistData;
import com.iheartradio.m3u8.data.StreamInfo;
import com.iheartradio.m3u8.data.StreamInfo.Builder;

abstract class MasterPlaylistTagWriter extends ExtTagWriter {
    
    // master playlist tags

    static final IExtTagWriter EXT_X_MEDIA = new MasterPlaylistTagWriter() {
        private final Map<String, AttributeWriter<MediaData>> HANDLERS = new HashMap<String, AttributeWriter<MediaData>>();

        {
            HANDLERS.put(Constants.TYPE, new AttributeWriter<MediaData>() {
                @Override
                public boolean containsAttribute(MediaData mediaData) {
                    return true;
                };
                
                @Override
                public String write(MediaData mediaData) throws ParseException {
                    return mediaData.getType().getValue();
                }
            });

            HANDLERS.put(Constants.URI, new AttributeWriter<MediaData>() {
                @Override
                public boolean containsAttribute(MediaData mediaData) {
                    return mediaData.hasUri();
                };

                @Override
                public String write(MediaData mediaData) throws ParseException {
                    return WriteUtil.writeQuotedString(mediaData.getUri(), getTag());
                }
            });

            HANDLERS.put(Constants.GROUP_ID, new AttributeWriter<MediaData>() {
                @Override
                public boolean containsAttribute(MediaData mediaData) {
                    return true;
                };
                
                @Override
                public String write(MediaData mediaData) throws ParseException {
                    return WriteUtil.writeQuotedString(mediaData.getGroupId(), getTag());
                }
                
            });

            HANDLERS.put(Constants.LANGUAGE, new AttributeWriter<MediaData>() {
                @Override
                public boolean containsAttribute(MediaData mediaData) {
                    return mediaData.hasLanguage();
                };

                @Override
                public String write(MediaData mediaData) throws ParseException {
                    return WriteUtil.writeQuotedString(mediaData.getLanguage(), getTag());
                }
            });

            HANDLERS.put(Constants.ASSOCIATED_LANGUAGE, new AttributeWriter<MediaData>() {
                @Override
                public boolean containsAttribute(MediaData mediaData) {
                    return mediaData.hasAssociatedLanguage();
                };

                @Override
                public String write(MediaData mediaData) throws ParseException {
                    return WriteUtil.writeQuotedString(mediaData.getAssociatedLanguage(), getTag());
                }
                
            });

            HANDLERS.put(Constants.NAME, new AttributeWriter<MediaData>() {
                @Override
                public boolean containsAttribute(MediaData mediaData) {
                    return true;
                };
                
                @Override
                public String write(MediaData mediaData) throws ParseException {
                    return WriteUtil.writeQuotedString(mediaData.getName(), getTag());
                }
            });

            HANDLERS.put(Constants.DEFAULT, new AttributeWriter<MediaData>() {
                @Override
                public boolean containsAttribute(MediaData mediaData) {
                    return true;
                }
                
                @Override
                public String write(MediaData mediaData) throws ParseException {
                    return WriteUtil.writeYesNo(mediaData.isDefault());
                }
            });

            HANDLERS.put(Constants.AUTO_SELECT, new AttributeWriter<MediaData>() {
                @Override
                public boolean containsAttribute(MediaData mediaData) {
                    return true;
                }
                
                @Override
                public String write(MediaData mediaData) throws ParseException {
                    return WriteUtil.writeYesNo(mediaData.isAutoSelect());
                }
            });

            HANDLERS.put(Constants.FORCED, new AttributeWriter<MediaData>() {
                @Override
                public boolean containsAttribute(MediaData mediaData) {
                    return true;
                }
                
                @Override
                public String write(MediaData mediaData) throws ParseException {
                    return WriteUtil.writeYesNo(mediaData.isForced());
                }
            });

            HANDLERS.put(Constants.IN_STREAM_ID, new AttributeWriter<MediaData>() {
                @Override
                public boolean containsAttribute(MediaData mediaData) {
                    return mediaData.hasInStreamId();
                }

                @Override
                public String write(MediaData mediaData) throws ParseException {
                    return WriteUtil.writeQuotedString(mediaData.getInStreamId(), getTag());
                }
            });

            HANDLERS.put(Constants.CHARACTERISTICS, new AttributeWriter<MediaData>() {
                @Override
                public boolean containsAttribute(MediaData mediaData) {
                    return mediaData.hasCharacteristics();
                }
                
                @Override
                public String write(MediaData mediaData) throws ParseException {
                    return WriteUtil.writeQuotedString(WriteUtil.join(mediaData.getCharacteristics(), Constants.ATTRIBUTE_LIST_SEPARATOR), getTag());
                }
            });
        }

        @Override
        public String getTag() {
            return Constants.EXT_X_MEDIA_TAG;
        }

        @Override
        boolean hasData() {
            return true;
        }
    };

    static abstract class EXT_STREAM_INF extends MasterPlaylistTagWriter {
        final Map<String, AttributeWriter<StreamInfo>> HANDLERS = new HashMap<String, AttributeWriter<StreamInfo>>();

        {
            HANDLERS.put(Constants.BANDWIDTH, new AttributeWriter<StreamInfo>() {
                @Override
                public boolean containsAttribute(StreamInfo streamInfo) {
                    return streamInfo.hasBandwidth();
                }
                
                @Override
                public String write(StreamInfo streamInfo) {
                    return Integer.toString(streamInfo.getBandwidth());
                }
            });

            HANDLERS.put(Constants.AVERAGE_BANDWIDTH, new AttributeWriter<StreamInfo>() {
                @Override
                public boolean containsAttribute(StreamInfo streamInfo) {
                    return streamInfo.hasAverageBandwidth();
                }
                
                @Override
                public String write(StreamInfo streamInfo)  {
                    return Integer.toString(streamInfo.getAverageBandwidth());
                }
            });

            HANDLERS.put(Constants.CODECS, new AttributeWriter<StreamInfo>() {
                @Override
                public boolean containsAttribute(StreamInfo streamInfo) {
                    return streamInfo.hasCodecs();
                }
                
                @Override
                public String write(StreamInfo streamInfo) throws ParseException {
                    return WriteUtil.writeQuotedString(WriteUtil.join(streamInfo.getCodecs(), Constants.ATTRIBUTE_LIST_SEPARATOR), getTag());
                }
            });

            HANDLERS.put(Constants.RESOLUTION, new AttributeWriter<StreamInfo>() {
                @Override
                public boolean containsAttribute(StreamInfo streamInfo) {
                    return streamInfo.hasResolution();
                }
                
                @Override
                public String write(StreamInfo streamInfo) throws ParseException {
                    return WriteUtil.writeResolution(streamInfo.getResolution());
                }
            });

            HANDLERS.put(Constants.VIDEO, new AttributeWriter<StreamInfo>() {
                @Override
                public boolean containsAttribute(StreamInfo streamInfo) {
                    return streamInfo.hasVideo();
                }
                
                @Override
                public String write(StreamInfo streamInfo) throws ParseException {
                    return ParseUtil.parseQuotedString(streamInfo.getVideo(), getTag());
                }
            });

            HANDLERS.put(Constants.PROGRAM_ID, new AttributeWriter<StreamInfo>() {
                @Override
                public boolean containsAttribute(StreamInfo streamInfo) {
                    return false;
                }
                
                @Override
                public String write(StreamInfo streamInfo) {
                    // deprecated
                    return "";
                }
            });
        }

        @Override
        boolean hasData() {
            return true;
        }

        protected abstract void handle(Builder builder, ParseState state, StreamInfo streamInfo) throws ParseException;

    }
    
    static final IExtTagWriter EXT_X_I_FRAME_STREAM_INF = new EXT_STREAM_INF() {
        
        {
            HANDLERS.put(Constants.URI, new AttributeWriter<StreamInfo>() {
                @Override
                public boolean containsAttribute(StreamInfo streamInfo) {
                    return streamInfo.hasUri();
                };
                
                @Override
                public String write(StreamInfo streamInfo) throws ParseException {
                    return WriteUtil.writeQuotedString(streamInfo.getUri(), getTag());
                }
            });
        }
        
        protected void handle(Builder streamBuilder, ParseState state, StreamInfo streamInfo) throws ParseException {

            if (!streamBuilder.isUriSet()) {
                throw new ParseException(ParseExceptionType.MISSING_STREAM_URI, getTag());
            }
            String line = streamInfo.getUri();
            
            final PlaylistData.Builder builder = new PlaylistData.Builder();

            if (Constants.URL_PATTERN.matcher(line).matches()) {
                builder.withUrl(line);
            } else {
                builder.withPath(line);
            }
            
            final MasterParseState masterState = state.getMaster();

            masterState.playlists.add(builder
                    .withStreamInfo(masterState.streamInfo)
                    .build());
        };
        
        @Override
        public String getTag() {
            return Constants.EXT_X_I_FRAME_STREAM_INF_TAG;
        } 
    };
    
    static final IExtTagWriter EXT_X_STREAM_INF = new EXT_STREAM_INF() {
        
        {
            HANDLERS.put(Constants.AUDIO, new AttributeWriter<StreamInfo>() {
                @Override
                public boolean containsAttribute(StreamInfo streamInfo) {
                    return streamInfo.hasAudio();
                }
                
                @Override
                public String write(StreamInfo streamInfo) throws ParseException {
                    return WriteUtil.writeQuotedString(streamInfo.getAudio(), getTag());
                }
            });
            
            HANDLERS.put(Constants.SUBTITLES, new AttributeWriter<StreamInfo>() {
                @Override
                public boolean containsAttribute(StreamInfo streamInfo) {
                    return streamInfo.hasSubtitles();
                }
                
                @Override
                public String write(StreamInfo streamInfo) throws ParseException {
                    return WriteUtil.writeQuotedString(streamInfo.getSubtitles(), getTag());
                }
            });

            HANDLERS.put(Constants.CLOSED_CAPTIONS, new AttributeWriter<StreamInfo>() {
                @Override
                public boolean containsAttribute(StreamInfo streamInfo) {
                    return streamInfo.hasClosedCaptions();
                }
                
                public String write(StreamInfo streamInfo) throws ParseException {
                    return WriteUtil.writeQuotedString(streamInfo.getClosedCaptions(), getTag());
                }
            });    
        }
        
        protected void handle(Builder builder, ParseState state, StreamInfo streamInfo) {
            state.getMaster().streamInfo = streamInfo;
        };
        
        @Override
        public String getTag() {
            return Constants.EXT_X_STREAM_INF_TAG;
        }
    };
}
