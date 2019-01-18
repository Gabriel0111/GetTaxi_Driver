package gabriel.gettaxi_driver.models.entities;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Driver implements Serializable {

    private String id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String creditCard;
    private String emailClient;
    private String UID;

    @Exclude
    private List<String> listClient = new LinkedList<>();

    //region ***** CONSTRUCTORS *****

    public Driver()
    {
        String[] firstNames = {"Gabriel", "Yaacov", "Jacques"};
        String[] lastNames = {"Elbaz", "Amar", "Marciano"};

        Random r = new Random(System.currentTimeMillis());

        firstName = firstNames[r.nextInt(firstNames.length)];
        lastName = lastNames[r.nextInt(lastNames.length)];
        id = String.valueOf(r.nextInt(1_000_000_000));
        phoneNumber = "053" + r.nextInt(1_000_000);
        email = (firstName + "." + lastName + "@gmail.com").toLowerCase();
        creditCard = "4580000000000000";
    }

    public Driver(String firstName, String lastName, String id,
                  String phoneNumber, String email, String creditCard) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.creditCard = creditCard;
    }

    //endregion

    //region ***** GETTERS/SETTERS *****

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

    public String getEmailClient() {
        return emailClient;
    }

    public void setEmailClient(String emailClient) {
        this.emailClient = emailClient;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    //endregion

    //region ***** ADMINISTRATION *****

    @Override
    public String toString() {
        return  "ID : " + id + ". " + '\n' +
                firstName + ' ' + lastName + '\n' +
                "Tel. " + phoneNumber + '\n' +
                "Email: " + email + '\n' +
                "CB: " + creditCard + '\n';
    }

    //endregion
}
