import React from 'react';
import Home from "./Home";
import './index.css';
import Login from './Login';
import Button from 'react-bootstrap/Button';
import Viewartists from './ViewArtists';

class AddArtist extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            token: this.props.token,
            id: this.props.id,
            uuid: "",
            name: "",
            active: "",
            artist: null,
            buttonPressed: "",
            errorMesage: ""
        };
        console.log(this.props);
        this.handleChange = this.handleChange.bind(this);
        this.makeAddArtistRequest = this.makeAddArtistRequest.bind(this);
        this.addArtistCbk = this.addArtistCbk.bind(this);
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

    handleChange(event) {
        let name = event.target.name;
        let value = event.target.value;
        let stateName = name.split("_")[1];

        if (stateName === "uuid") {
            this.setState({ uuid: value });
        }
        else if (stateName === "name") {
            this.setState({ name: value });
        }
        else if (stateName === "active") {
            if (value === "false") {
                this.setState({ active: false });
            }
            else if (value === "true") {
                this.setState({ active: true });
            }
        }
    }

    makeAddArtistRequest(uuid, name, active, token) {
        return new Promise(function (resolve, reject) {
            let message = JSON.stringify({
                "uuid": uuid,
                "name": name,
                "active": active
            });
            console.log(message);

            let xmlHttp = new XMLHttpRequest();
            xmlHttp.open("PUT", `http://localhost:8080/api/songcollection/artists/${uuid}`, true);

            // let responseMessage;
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState === 4) {
                    if (xmlHttp.status === 201) {
                        alert("Adaugarea s-a efectuat cu succes!");
                        // un redirect catre componenta de view artists
                        resolve(201);
                    }
                    else if (xmlHttp.status === 204) {
                        alert("Update-ul s-a realizat cu succes");
                        resolve(204);
                    }
                    else if (xmlHttp.status === 403) {
                        reject("Forbidden");
                    }
                    else if (xmlHttp.status === 401) {
                        console.log(xmlHttp.responseText);
                        // token-ul de login a expirat - se va face un relogin
                        if (xmlHttp.responseText.includes("token login")) {
                            reject("Expired");
                        }
                    }
                    else {
                        alert(xmlHttp.status + " - " + xmlHttp.responseText);
                    }
                }
            }
            xmlHttp.setRequestHeader('Content-Type', 'application/json');
            xmlHttp.setRequestHeader('Authorization', 'Bearer ' + token);
            xmlHttp.send(message);
        });
    };

    async addArtistCbk(event) {
        event.preventDefault();

        this.makeAddArtistRequest(this.state.uuid, this.state.name, this.state.active, this.state.token)
            .then(
                (res) => {
                    console.log(res);
                    if(res === 201 || res === 204)
                    this.setState({buttonPressed: "viewArtists"});
                }
            )
            .catch((err) => {
                console.log(err);
                if (err.includes("Forbidden")) {
                    this.setState({ errorMesage: "Forbidden" });
                     // se apeleaza logout pt invalidarea token-ului
                    this.logout(this.state.token);
                }
                else {
                    this.requestLoginToken();
                }
            })
    }

    setViewMusicDestination(message) {
        this.setState({ buttonPressed: message });
    }

    getSnapshotBeforeUpdate(prevProps, prevState) {
        return prevState;
    }


    shouldComponentUpdate(prevProps, prevState) {
        // cererea de add s-a executat cu succes | a fost apasat butonul de back | status code 403 -> redirect pe login
        if (prevState.artist != null || prevState.buttonPressed !== '' || prevState.errorMesage !== '') {
            return true;
        } else {
            return false;
        }
    }

    render() {
        if (this.state.errorMesage === "Forbidden") {
            return (
                <Login />
            );
        }
        if (this.state.artist != null) {
            return (
                <Home token={this.state.token}
                    username={this.props.username}
                    password={this.props.password}
                    id={this.props.id} />
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
        else if(this.state.buttonPressed === "viewArtists") {
            return (
                <Viewartists token={this.state.token}
                    username={this.props.username}
                    password={this.props.password}
                    id={this.props.id} />
            );
        }
        return (
            <div>
                <form id="add_artist_form" onSubmit={this.addArtistCbk}>
                    <input className="login_text" type="text" placeholder="uuid" id="uuid" name="artist_uuid" onChange={this.handleChange} required></input>
                    <br></br>
                    <input className="login_text" type="text" placeholder="name" id="name" name="artist_name" onChange={this.handleChange}></input>
                    <br></br>
                    <input className="login_text" type="text" placeholder="active(true/false)" id="active" name="artist_active" onChange={this.handleChange}></input>
                    <br></br>
                    <input className="login_text" type="submit" value="Add artist"></input>
                </form>
                <p className="login_text" id="login_error_message"> {this.state.errorMesage}</p>

                <Button className="button" onClick={(e) => this.setViewMusicDestination("home")}>Back</Button>
            </div>
        );
    }
}

export default AddArtist;