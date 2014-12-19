package jm.net.ac;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

public class YoutubeComment {
    public static class Builder {
        private String mTitle = "";
        private String mText  = "";
        private String mAuthor = "";
        public Builder title(@Nonnull String title){
            mTitle = checkNotNull(title);
            return this;
        }
        public Builder text(@Nonnull String text){
            mText = checkNotNull(text);
            return this;
        }
        public Builder author(@Nonnull String author){
            mAuthor = checkNotNull(author);
            return this;
        }
        public YoutubeComment build(){
            return new YoutubeComment(mTitle, mAuthor, mText);
        }
    }

    public final String title;
    public final String text;
    public final String author;

    private YoutubeComment(String title, String text, String author){
        this.title = title;
        this.text = text;
        this.author = author;
    }

    public String toString(){
        return (title.isEmpty() ? "" : title + ":\n") +
               text  +
                (author.isEmpty() ? "" : "\n - " + author);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YoutubeComment)) return false;
        YoutubeComment that = (YoutubeComment) o;
        return author.equals(that.author) && text.equals(that.text) && title.equals(that.title);

    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + text.hashCode();
        result = 31 * result + author.hashCode();
        return result;
    }
}
