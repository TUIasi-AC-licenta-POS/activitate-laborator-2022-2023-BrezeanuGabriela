import React from 'react';
import Home from "./Home";
import './index.css';
import $ from 'jquery'; 

class Login extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            username: "",
            password: "",
            id: "",
            token: "",
            tokenReceived: false,
            errorMesage: ""
        };

        this.handleChange = this.handleChange.bind(this);
        this.requestLoginToken = this.requestLoginToken.bind(this);
        this.render = this.render.bind(this);
        this.setToken = this.setToken.bind(this);
        this.makeSoapRequest = this.makeSoapRequest.bind(this);
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
    }

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
                        }
                        else {
                            document.getElementById("login_username").value = "";
                            document.getElementById("login_password").value = "";
                            
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
            // xmlHttp.setRequestHeader("Acces-Control-Allow-Origin", '*');
            xmlHttp.send(message);
        });
    }

    async requestLoginToken(event) {
        event.preventDefault();
        
        let username = this.state.username;
        let password = this.state.password;

        // let message =
        //         "<soap11env:Envelope xmlns:soap11env=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:sample=\"services.spotify.idm.soap\">" +
        //         "<soap11env:Body>" +
        //         "<sample:Login>" +
        //         "<sample:username>" + username + "</sample:username>" +
        //         "<sample:password>" + password + "</sample:password>" +
        //         "</sample:Login>" +
        //         "</soap11env:Body>" +
        //         "</soap11env:Envelope>";


        this.makeSoapRequest(this.state.username, this.state.password)
            .then(
                (res) => {
                    this.setToken(res);
                }
            ).catch( err => {
                this.setState({errorMesage : err});
            });
    }

    setToken(responseMessage) {
        this.setState({ token: responseMessage.split("#")[0] });
        this.setState({ id: parseInt(responseMessage.split("#")[1])});
        this.setState({ tokenReceived: true });
    }

    render() {
        if (this.state.tokenReceived === false) {
            return (
                <div>
                    <form id="login_form" onSubmit={this.requestLoginToken}>
                        <input className="login_text" type="text" placeholder="username" id="login_username" name="login_username" onChange={this.handleChange}></input>
                        <br></br>
                        <input className="login_text" type="password" placeholder="password" id="login_password" name="login_password" onChange={this.handleChange}></input>
                        <br></br>
                        <input className="login_text" type="submit"></input>
                    </form>
                    <p className="login_text" id="login_error_message"> {this.state.errorMesage}</p>
                </div>
            );
        }
        else {
            return (<Home   token={this.state.token}
                            username={this.state.username}
                            password={this.state.password} 
                            id={this.state.id}
                    />);
        }
    }
}

// trebuie exportata fiecare componenta pe care o folosesc intr-un fisier
export default Login;