public class SongObject {

    private String artistName;
    private String songName;
    private long duration;
    private String fileName;

    public SongObject(String artistName, String songName, long duration, String fileName) {
        this.songName = songName;
        this.artistName = artistName;
        this.duration = duration;
        this.fileName = fileName;
    }
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
    public String getArtistName() {
        return this.artistName;
    }
    public void setSongName(String songName) {
        this.songName = songName;
    }
    public String getSongName() {
        return this.songName;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }
    public long getDuration() {
        return this.duration;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileName() {
        return this.fileName;
    }
    @Override
    public String toString() {
        String result = "";
        String artistNameStr = "Artist name: " + this.artistName + "\n";
        String songNameStr = "Song name: " + this.songName + "\n";
        String durationStr = "Duration: " + String.valueOf(this.duration) + "\n";
        result += songNameStr;
        result += artistNameStr;
        result += durationStr;
        return result;
    }
}
