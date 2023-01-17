import React from 'react';
import Home from "./Home";
import './index.css';
import Forbidden from './Forbidden';

class AddUser extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            username: "",
            password: "",
            roles: "",
            idReceived: "",
            id: this.props.id,
            token: this.props.token,
            errorMesage: ""
        };

        this.handleChange = this.handleChange.bind(this);
        this.requestAddUser = this.requestAddUser.bind(this);
        this.render = this.render.bind(this);
        this.setIdReceived = this.setIdReceived.bind(this);
        this.makeSoapRequest = this.makeSoapRequest.bind(this);
        this.logout.bind(this);
    }

    handleChange(event) {
        let name = event.target.name;
        let value = event.target.value;
        let stateName = name.split("_")[1];

        if (stateName === "username") {
            this.setState({ 'username': value });
        }
        else if (stateName === "password") {
            this.setState({ "password": value });
        }
        else if (stateName === "roles") {
            this.setState({ "roles": value });
        }
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

    makeSoapRequest(username, password, token, roles) {
        return new Promise(function (resolve, reject) {
            let xmlHttp = new XMLHttpRequest();
            xmlHttp.open("POST", "http://127.0.0.1:8000", true);

            let message =
                "<soap11env:Envelope xmlns:soap11env=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sample=\"services.spotify.idm.soap\">" +
                "<soap11env:Body>" +
                "<sample:CreateUser>" +
                "<sample:token>" + token + "</sample:token>" +
                "<sample:username>" + username + "</sample:username>" +
                "<sample:password>" + password + "</sample:password>" +
                "<sample:roles>" + roles + "</sample:roles>" +
                "</sample:CreateUser>" +
                "</soap11env:Body>" +
                "</soap11env:Envelope>";

            let responseMessage;
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState === 4) {
                    if (xmlHttp.status === 200) {
                        // console.log(xmlHttp.responseText)
                        var XMLParser = require('react-xml-parser');
                        var xml = new XMLParser().parseFromString(xmlHttp.responseText);
                        let response = xml.children[0].children[0].children[0];
                        responseMessage = response.value;
                        console.log(responseMessage)
                        // s-a primit token-ul
                        if (responseMessage.split("Error:").length === 1) {
                            resolve(responseMessage);
                        }
                        else {
                            document.getElementById("login_username").value = "";
                            document.getElementById("login_password").value = "";
                            document.getElementById("login_roles").value = "";

                            reject(responseMessage.split("Error:")[1]);
                        }
                    }
                    else {
                        console.log(xmlHttp.status);
                        console.log(xmlHttp.responseText);
                        alert("Reminder: Porneste serviciul SOAP! (+ server-ul sql)");
                    }
                }
            }
            xmlHttp.send(message);
        });
    }

    getSnapshotBeforeUpdate(prevProps, prevState) {
        return prevState;
    }


    shouldComponentUpdate(prevProps, prevState) {
        // se face update doar daca s-a primit raspunsul de la soap si s-a setat token-ul sau un mesaj de eroare
        if (prevState.idReceived !== '' || prevState.errorMesage !== '') {
            return true;
        } else {
            return false;
        }
    }

    async requestAddUser(event) {
        event.preventDefault();

        this.makeSoapRequest(this.state.username, this.state.password, this.state.token, this.state.roles)
            .then(
                (res) => {
                    this.setIdReceived(res);
                }
            ).catch(err => {
                this.setState({ errorMesage: err });
                // nu este administrator
                if(err === "fara drept")
                {
                    this.setState({ errorMesage: "Forbidden" });
                    this.logout(this.state.token);
                }
            });
    }

    setIdReceived(responseMessage) {
        this.setState({ idReceived: responseMessage});
    }

    render() {
        if(this.state.errorMesage === "Forbidden")
        {
            return (
                <Forbidden />
            );
        }

        if (this.state.idReceived === "") {
            return (
                <div>
                    <form id="login_form" onSubmit={this.requestAddUser}>
                        <input className="login_text" type="text" placeholder="username" id="login_username" name="login_username" onChange={this.handleChange}></input>
                        <br></br>
                        <input className="login_text" type="password" placeholder="password" id="login_password" name="login_password" onChange={this.handleChange}></input>
                        <br></br>
                        <p>Rolurile posibile: client, content_manager, administrator, artist. Daca doriti sa fie mai multe, inserati de exemplu: "client-artist"</p>
                        <input className="login_text" type="text" placeholder="roles" id="login_roles" name="login_roles" onChange={this.handleChange}></input>
                        <br></br>
                        <input className="login_text" type="submit"></input>
                    </form>
                    <p className="login_text" id="login_error_message"> {this.state.errorMesage}</p>
                </div>
            );
        }

        return (
                <Home token={this.props.token}
                    username={this.props.username}
                    password={this.props.password} 
                    id={this.props.id}/>
            );
    }
}

// trebuie exportata fiecare componenta pe care o folosesc intr-un fisier
export default AddUser;