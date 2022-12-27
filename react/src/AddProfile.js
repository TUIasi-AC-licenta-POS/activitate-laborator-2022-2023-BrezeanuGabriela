import React from 'react';
import Button from 'react-bootstrap/Button';
import Login from './Login';
import Home from './Home';

class AddProfile extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            token: this.props.token,
            id: this.props.id,
            firstName: "",
            lastName: "",
            email: "",
            profile: null,
            buttonPressed: "",
            errorMesage: ""
        };

        console.log(this.props);
        this.handleChange = this.handleChange.bind(this);
        this.makeAddProfileRequest = this.makeAddProfileRequest.bind(this);
        this.addProfileCbk = this.addProfileCbk.bind(this);
        this.logout = this.logout.bind(this);
        this.requestLoginToken = this.requestLoginToken.bind(this);
    }

    logout(token) {
        let xmlHttp = new XMLHttpRequest();
        xmlHttp.open("POST", "http://127.0.0.1:8000", true);

        let message =
            "<soap11env:Envelope xmlns:soap11env=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sample=\"services.spotify.idm.soap\">" +
            "<soap11env:Body>" +
            "<sample:Logout>" +
            "<sample:token>" + token + "</sample:token>" +
            "</sample:Logout>" +
            "</soap11env:Body>" +
            "</soap11env:Envelope>";

        // let responseMessage;
        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState === 4) {
                if (xmlHttp.status === 200) {
                    alert("Veti fi redirectat catre Login");
                }
            }
        }
        xmlHttp.send(message);
    }

    //relogin
    makeSoapRequest(username, password) {
        return new Promise(function (resolve, reject) {
            let xmlHttp = new XMLHttpRequest();
            xmlHttp.open("POST", "http://127.0.0.1:8000", true);

            let message =
                "<soap11env:Envelope xmlns:soap11env=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sample=\"services.spotify.idm.soap\">" +
                "<soap11env:Body>" +
                "<sample:Login>" +
                "<sample:username>" + username + "</sample:username>" +
                "<sample:password>" + password + "</sample:password>" +
                "</sample:Login>" +
                "</soap11env:Body>" +
                "</soap11env:Envelope>";

            let responseMessage;
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState === 4) {
                    if (xmlHttp.status === 200) {
                        var XMLParser = require('react-xml-parser');
                        var xml = new XMLParser().parseFromString(xmlHttp.responseText);
                        let response = xml.children[0].children[0].children[0];
                        responseMessage = response.value;
                        if (responseMessage.split("Error:").length === 1) {
                            resolve(responseMessage);
                            document.getElementById("name").value = "";
                            document.getElementById("uuid").value = "";
                            document.getElementById("active").value = "";

                        }
                        else {
                            document.getElementById("name").value = "";
                            document.getElementById("uuid").value = "";
                            document.getElementById("active").value = "";

                            reject(responseMessage.split("Error:")[1]);
                        }
                    }
                    else {
                        alert("Reminder: Porneste serviciul SOAP! (+ server-ul sql)");
                    }
                }
            }
            xmlHttp.send(message);
        });
    }

    async requestLoginToken() {
        this.makeSoapRequest(this.props.username, this.props.password)
            .then(
                (res) => {
                    this.setToken(res);
                    alert("Ne pare rau, a aparut o eroare!(token expirat) Mai incercati o data!");
                }
            ).catch(err => {
                this.setState({ errorMesage: err });
            });
    }

    setToken(responseMessage) {
        this.setState({ token: responseMessage.split("#")[0] });
        this.setState({ id: parseInt(responseMessage.split("#")[1]) });
    }

    setViewMusicDestination(message) {
        this.setState({ buttonPressed: message });
    }

    handleChange(event) {
        let name = event.target.name;
        let value = event.target.value;
        let stateName = name.split("_")[1];

        if (stateName === "firstName") {
            this.setState({ firstName: value });
        }
        else if (stateName === "lastName") {
            this.setState({ lastName: value });
        }
        else if (stateName === "email") {
            this.setState({ email: value });
        }
    }

    makeAddProfileRequest(firstName, lastName, email, token, idUser) {
        return new Promise(function (resolve, reject) {
            console.log(idUser);
            let message = JSON.stringify({
                "idUser": idUser,
                "firstName": firstName,
                "lastName": lastName,
                "email": email,
                "likedMusic": []
            });
            console.log(message);

            let xmlHttp = new XMLHttpRequest();
            xmlHttp.open("POST", `http://localhost:8081/api/profiles/`, true);

            // let responseMessage;
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState === 4) {
                    if (xmlHttp.status === 201) {
                        alert("Adaugarea s-a efectuat cu succes!");
                        resolve(201);
                    }
                    else if (xmlHttp.status === 401) {
                        console.log(xmlHttp.responseText);
                        // token-ul de login a expirat - se va face un relogin
                        if (xmlHttp.responseText.includes("token login")) {
                            reject("Expired");
                        }
                    }
                    else if (xmlHttp.status === 403) {
                        console.log(xmlHttp.status);
                        // reject("Forbidden");
                    }
                    // il las tot pe user tot pe form pentru a-si corecta datele
                    else if (xmlHttp.status === 409) {
                        alert(xmlHttp.responseText);
                    }
                    else if (xmlHttp.status === 422) {
                        alert(xmlHttp.responseText);
                    }
                    else if (xmlHttp.status === 406) {
                        alert(xmlHttp.responseText);
                    }
                    else {
                        console.log(xmlHttp.status);
                        alert(xmlHttp.responseText);
                    }
                }
            }
            xmlHttp.setRequestHeader('Content-Type', 'application/json');
            xmlHttp.setRequestHeader('Authorization', 'Bearer ' + token);
            xmlHttp.send(message);
        });
    };

    async addProfileCbk(event) {
        event.preventDefault();

        this.makeAddProfileRequest(this.state.firstName, this.state.lastName, this.state.email, this.state.token, this.state.id)
            .then(
                (res) => {
                    console.log(res);
                    // if(res === 201 || res === 204)
                    // this.setState({buttonPressed: "viewSongs"});
                }
            )
            .catch((err) => {
                console.log(err);
                if (err.includes("Forbidden")) {
                    this.setState({ errorMesage: "Forbidden" });
                }
                else {
                    this.requestLoginToken();
                }
            })
    }

    render() {
        if (this.state.errorMesage === "Forbidden") {
            // ar mai trebui apelata si metoda de log out pt invalidarea token-ului
            this.logout(this.state.token);
            return (
                <Login />
            );
        }
        if (this.state.buttonPressed === "home") {
            return (
                <Home token={this.state.token}
                    username={this.props.username}
                    password={this.props.password}
                    id={this.props.id} />
            );
        }
        return (
            <div>
                <form id="add_profile_form" onSubmit={this.addProfileCbk}>
                    <input className="login_text" type="text" placeholder="first name" id="firstName" name="profile_firstName" onChange={this.handleChange}></input>
                    <br></br>
                    <input className="login_text" type="text" placeholder="last name" id="lastName" name="profile_lastName" onChange={this.handleChange}></input>
                    <br></br>
                    <input className="login_text" type="email" placeholder="email" id="email" name="profile_email" onChange={this.handleChange}></input>
                    <br></br>
                    <input className="login_text" type="submit" value="Add Profile"></input>
                </form>
                <p className="login_text" id="login_error_message"> {this.state.errorMesage}</p>

                <Button className="button" onClick={(e) => this.setViewMusicDestination("home")}>Back</Button>
            </div>
        );
    }

}

export default AddProfile;
