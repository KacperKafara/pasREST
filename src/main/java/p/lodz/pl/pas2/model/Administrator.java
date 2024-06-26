package p.lodz.pl.pas2.model;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.UUID;

@NoArgsConstructor
public class Administrator extends User {
    public Administrator(String username, boolean active, String password) {
        super(username, active, password);
    }
    public Administrator(UUID id,
                         String username,
                         boolean active,
                         String password) {
        super(id, username, active, password);
    }
}
