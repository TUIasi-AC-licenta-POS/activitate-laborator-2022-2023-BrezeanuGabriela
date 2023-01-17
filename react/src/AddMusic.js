import React from 'react';
import Home from "./Home";
import './index.css';
import Button from 'react-bootstrap/Button';
import Viewsongs from './ViewSongs';
import Forbidden from './Forbidden';
import Login from './Login';

class AddMusic extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            token: this.props.token,
            id: this.props.id,
            name: "",
            year: "",
            type: "",
            genre: "",
            idAlbum: "",
            idArtist: "",
            idsArtist: [],
            artist: null,
            buttonPressed: "",
            errorMesage: ""
        };

        console.log(this.props);
        this.handleChange = this.handleChange.bind(this);
        this.makeAddMusicRequest = this.makeAddMusicRequest.bind(this);
        this.addMusicCbk = this.addMusicCbk.bind(this);
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

        xmlHttp.onreadystatechange = function () {
            if (xmlHttp.readyState === 4) {
                if (xmlHttp.status === 200) {
                    // alert("Veti fi redirectat catre Login");
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

        if (stateName === "name") {
            this.setState({ name: value });
        }
        else if (stateName === "year") {
            this.setState({ year: value });
        }
        else if (stateName === "type") {
            this.setState({ type: value });
        }
        else if (stateName === "genre") {
            this.setState({ genre: value });
        }
        else if (stateName === "idAlbum") {
            this.setState({ idAlbum: value });
        }
        else if (stateName === "idArtist") {
            this.setState({ idArtist: value });
        }
    }

    makeAddMusicRequest(name, year, type, genre, idAlbum, idArtist, token) {
        return new Promise(function (resolve, reject) {
            let message = JSON.stringify({
                "name": name,
                "genre": genre,
                "year": year,
                "type": type,
                "idAlbum": idAlbum,
                "idsArtist": [idArtist]
            });
            console.log(message);

            let xmlHttp = new XMLHttpRequest();
            xmlHttp.open("POST", `http://localhost:8080/api/songcollection/songs/`, true);

            // let responseMessage;
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState === 4) {
                    if (xmlHttp.status === 201) {
                        alert("Adaugarea s-a efectuat cu succes!");
                        resolve(201);
                    }
                    else if (xmlHttp.status === 204) {
                        alert("Update-ul s-a realizat cu succes");
                        resolve(204);
                    }
                    else if (xmlHttp.status === 403) {
                        console.log(xmlHttp.status);
                        reject("Forbidden");
                    }
                    else if (xmlHttp.status === 401) {
                        console.log(xmlHttp.responseText);
                        // token-ul de login a expirat - se va face un relogin
                        if (xmlHttp.responseText.includes("token login")) {
                            reject("Expired");
                        }
                        else {
                            reject("Invalid");
                        }
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

    async addMusicCbk(event) {
        event.preventDefault();

        this.makeAddMusicRequest(this.state.name, this.state.year, this.state.type, this.state.genre, this.state.idAlbum, this.state.idArtist, this.state.token)
            .then(
                (res) => {
                    console.log(res);
                    if (res === 201 || res === 204)
                        this.setState({ buttonPressed: "viewSongs" });
                }
            )
            .catch((err) => {
                console.log(err);
                if (err.includes("Forbidden")) {
                    this.setState({ errorMesage: "Forbidden" });
                    // ar mai trebui apelata si metoda de log out pt invalidarea token-ului
                    this.logout(this.state.token);
                }
                else if(err.includes("Expired")){
                    this.requestLoginToken();
                }
                else if(err.includes("Invalid")) {
                    this.setState({ errorMesage: "Invalid"});
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
        if (prevState.buttonPressed !== '' || prevState.errorMesage !== '') {
            return true;
        } else {
            return false;
        }
    }

    render() {
        console.log("render");
        if (this.state.errorMesage === "Forbidden") {
            return (
                <Forbidden />
            );
        }
        else if (this.state.errorMesage === "Invalid") {
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
        else if (this.state.buttonPressed === "viewSongs") {
            return (
                <Viewsongs token={this.state.token}
                    username={this.props.username}
                    password={this.props.password}
                    id={this.props.id} />
            );
        }
        return (
            <div>
                <form id="add_music_form" onSubmit={this.addMusicCbk}>
                    <input className="login_text" type="text" placeholder="name" id="name" name="music_name" onChange={this.handleChange} required></input>
                    <br></br>
                    <input className="login_text" type="number" placeholder="year" id="year" name="music_year" onChange={this.handleChange}></input>
                    <br></br>
                    <input className="login_text" type="text" placeholder="type(song/single/album)" id="type" name="music_type" onChange={this.handleChange}></input>
                    <br></br>
                    <input className="login_text" type="text" placeholder="genre(rock/metal/pop)" id="type" name="music_genre" onChange={this.handleChange}></input>
                    <br></br>
                    <input className="login_text" type="text" placeholder="id album" id="type" name="music_idAlbum" onChange={this.handleChange}></input>
                    <br></br>
                    <input className="login_text" type="text" placeholder="id artist" id="type" name="music_idArtist" onChange={this.handleChange}></input>
                    <br></br>
                    <input className="login_text" type="submit" value="Add music"></input>
                </form>
                <p className="login_text" id="login_error_message"> {this.state.errorMesage}</p>

                <Button className="button" onClick={(e) => this.setViewMusicDestination("home")}>Back</Button>
            </div>
        );
    }
}

export default AddMusic;