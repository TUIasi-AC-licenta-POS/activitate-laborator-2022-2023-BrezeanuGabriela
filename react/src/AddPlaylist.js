import React from 'react';
import Home from "./Home";
import './index.css';
import Forbidden from './Login';
import Button from 'react-bootstrap/Button';
import AddProfile from './AddProfile';
import Login from './Login';

class AddPlaylist extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            token: this.props.token,
            id: this.props.id,
            items: [],
            playlistName: "",
            visibility: "",
            songs: [],
            selectedSongsName: [],
            idProfile: "",
            buttonPressed: "",
            errorMesage: ""
        };

        console.log(this.props);
        this.handleChange = this.handleChange.bind(this);
        this.makeAddPlaylistRequest = this.makeAddPlaylistRequest.bind(this);
        this.makeAddPlaylistToProfileRequest = this.makeAddPlaylistToProfileRequest.bind(this);
        this.getUserProfileRequest = this.getUserProfileRequest.bind(this);
        this.addPlaylistCbk = this.addPlaylistCbk.bind(this);
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

    componentDidMount() {
        fetch(
            "http://localhost:8080/api/songcollection/songs/",
            {
                method: 'get',
                mode: 'cors',
                headers: {
                    'Access-Control-Allow-Origin': '*'
                }
            })
            .then((res) => {
                return res.json()
            })
            .then((json) => {
                let _embedded = json._embedded;
                let musics = _embedded.musics;
                // let _links = json._links;
                this.setState({
                    items: musics,
                    isLoaded: true
                });
            })
            .catch((error) => {
                alert("Daca vrei sa mearga, porneste si Rest Sportify(SQL)!");
            });

        // preluam profile-ul
        this.getUserProfileRequest(this.state.id)
            .then(
                (res) => {
                    let profile = JSON.parse(res);
                    let idProfile = profile._links.self.href.split("?")[0].split("/").at(-1);
                    this.setState({ idProfile: idProfile });
                }
            )
            // profilul nu exista
            .catch((err) => {
                console.log(err);
                alert("Nu aveti un profile asociat! Veti fi redirectat catre pagina pentru crearea unui profile!");
                this.setState({ errorMesage: "Profile does not exist!" });
            });
    }

    setViewMusicDestination(message) {
        this.setState({ buttonPressed: message });
    }

    handleChange(event) {
        let name = event.target.name;
        let value = event.target.value;
        let stateName = name.split("_")[1];

        if (stateName === "name") {
            this.setState({ playlistName: value });
        }
        if (stateName === "visibility") {
            this.setState({ visibility: value });
        }
        if (stateName === "songs") {
            if (value !== "") {
                let selectedSongsName = this.state.selectedSongsName;
                selectedSongsName.push(value);
                this.setState({ selectedSongsName: selectedSongsName });

                document.getElementById("selectedSongs").innerText = selectedSongsName;

                let songs = this.state.songs;
                // get id for selected song
                let infoSong = Object.entries(this.state.items).filter(song => song[1]['name'] === value)[0][1];
                let selfLink = infoSong._links.self.href;
                let idSong = selfLink.split("/").at(-1);

                // update songs list
                songs.push({ "id": idSong });
                this.setState({ songs: songs });
            }
        }
    }

    makeAddPlaylistRequest(idUser, playlistName, visibility, songs, token) {
        return new Promise(function (resolve, reject) {
            let message = JSON.stringify({
                "idUser": idUser,
                "playlistName": playlistName,
                "visibility": visibility,
                "songs": songs
            });
            console.log(message);

            let xmlHttp = new XMLHttpRequest();
            xmlHttp.open("POST", `http://localhost:8081/api/playlists/`, true);

            // let responseMessage;
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState === 4) {
                    if (xmlHttp.status === 201) {
                        alert("Adaugarea s-a efectuat cu succes!");
                        // un redirect catre componenta de view artists
                        resolve(xmlHttp.responseText);
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

    getUserProfileRequest(idUser) {
        return new Promise(function (resolve, reject) {
            let xmlHttp = new XMLHttpRequest();
            xmlHttp.open("GET", `http://localhost:8081/api/profiles/` + idUser + '?field=idUser', true);

            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState === 4) {
                    if (xmlHttp.status === 200) {
                        resolve(xmlHttp.responseText);
                    }
                    // 404 - not found
                    else {
                        alert(xmlHttp.status + " - " + xmlHttp.responseText);
                        reject(xmlHttp.responseText);
                    }
                }
            }
            xmlHttp.send();
        });
    };

    makeAddPlaylistToProfileRequest(idProfile, idPlaylist, token) {
        return new Promise(function (resolve, reject) {
            let xmlHttp = new XMLHttpRequest();
            xmlHttp.open("PATCH", `http://localhost:8081/api/profiles/` + idProfile + '/playlists/' + idPlaylist + '?operation=add', true);

            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState === 4) {
                    if (xmlHttp.status === 204) {
                        alert("Adaugarea playlist-ului la profile s-a efectuat cu succes!");
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
                        else {
                            reject("Invalid");
                        }
                    }
                    else {
                        alert(xmlHttp.status + " - " + xmlHttp.responseText);
                    }
                }
            }
            xmlHttp.setRequestHeader('Content-Type', 'application/json');
            xmlHttp.setRequestHeader('Authorization', 'Bearer ' + token);
            xmlHttp.send();
        });
    };

    async addPlaylistCbk(event) {
        event.preventDefault();

        this.makeAddPlaylistRequest(this.state.id, this.state.playlistName, this.state.visibility, this.state.songs, this.state.token)
            .then(
                (res) => {
                    console.log(res);
                    let playlist = JSON.parse(res);
                    let idPlaylist = playlist._links.self.href.split("?")[0].split("/").at(-1);

                    // s-a inserat playlist-ul cu succes -> il asociem profile-ului
                    this.makeAddPlaylistToProfileRequest(this.state.idProfile, idPlaylist, this.state.token)
                        .then(
                            (res) => {
                                console.log(res);
                            }
                        )
                        .catch((err) => {
                            console.log(err);
                        });
                }
            )
            .catch((err) => {
                console.log(err);
                if (err.includes("Forbidden")) {
                    this.setState({ errorMesage: "Forbidden" });
                    // se apeleaza logout pt invalidarea token-ului
                    this.logout(this.state.token);
                }
                else if(err.includes("Expired")){
                    this.requestLoginToken();
                }
                else if(err.includes("Invalid")) {
                    this.setState({ errorMesage: "Invalid"});
                }
            });
    }

    render() {
        // console.log(this.state);
        if (this.state.buttonPressed === "home") {
            return (
                <Home token={this.props.token}
                    username={this.props.username}
                    password={this.props.password}
                    id={this.props.id} />
            );
        }
        else if (!this.state.isLoaded) {
            return (
                <>
                    <p> Asteptati sa se incarce piesele. </p>
                    <Button className="button" onClick={(e) => this.setViewMusicDestination("home")}>Back</Button>
                </>
            )
        }
        else if (this.state.errorMesage === "Profile does not exist!") {
            return (
                <AddProfile token={this.state.token}
                    username={this.props.username}
                    password={this.props.password}
                    id={this.props.id} />
            );
        }
        else if (this.state.errorMesage === "Forbidden") {
            return (
                <Forbidden />
            );
        }
        else if (this.state.errorMesage === "Invalid") {
            return (
                <Login />
            );
        }
        else {
            let options = [];
            options.push({ "value": "", label: "Select a song" });
            if (this.state.items !== "") {
                let optionsSongs = Object.entries(this.state.items).map(song => song[1]['name']);
                optionsSongs = optionsSongs.map((x) => ({ "value": x, "label": x }));
                options.push(...optionsSongs);
            }
            // console.log(this.state);

            return (
                <div className="App" >

                    <h1 > Music from Rest </h1>
                    <table >
                        <tr >
                            <th> Name </th>
                            <th> Genre</th>
                            <th> Type</th>
                            <th> Year</th>
                        </tr >
                        {

                            this.state.items.map((item) => (


                                <tr>
                                    <td > {item.name} </td>
                                    <td > {item.genre} </td>
                                    <td > {item.type} </td>
                                    <td > {item.year} </td>
                                </tr>
                            ))
                        }
                    </table>
                    <br></br>
                    <h3>Add playlist:</h3>
                    <form id="add_playlist_form" onSubmit={this.addPlaylistCbk}>
                        <input className="login_text" type="text" placeholder="playlistName" id="name" name="playlist_name" onChange={this.handleChange} required></input>
                        <br></br>
                        <label for="visibility" className="login_text">Visibility must be: pprivate/ppublic/friend</label>
                        <input className="login_text" type="text" placeholder="playlistVisibility" id="visibility" name="playlist_visibility" onChange={this.handleChange} required></input>
                        <br></br>
                        <p className="login_text"> Add song to playlist(just select them):</p>
                        <select onChange={this.handleChange} name="playlist_songs" className="login_text">
                            {
                                options.map((option) => (
                                    <option value={option.value}>{option.label}</option>
                                ))
                            }
                        </select>
                        <br></br>
                        <p>Selected songs:</p>
                        <pre id="selectedSongs" className="login_text"></pre>
                        <input className="login_text" type="submit" value="Add playlist"></input>
                    </form>
                    <Button className="button" onClick={(e) => this.setViewMusicDestination("home")}>Back</Button>
                </div >
            );
        }
    }
}

export default AddPlaylist;