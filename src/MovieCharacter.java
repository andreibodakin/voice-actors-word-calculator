
public class MovieCharacter {
    String name;
    int words;

    MovieCharacter() {
        this.name = null;
        this.words = 0;
    }

    MovieCharacter(String name) {
        this.name = name;
        this.words = 0;
    }

    MovieCharacter(String name, int words) {
        this.name = name;
        this.words = words;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MovieCharacter other = (MovieCharacter) obj;
        if (name.equals(other.name)) {
            this.words += other.words;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
//        result = prime * result + words;
        return result;
    }

    public String getName() {
        return this.name;
    }

    public int getWords() {
        return this.words;
    }

    public void setWords(int words) {
        this.words = words;
    }
}
