import React from 'react';
import Home from "./Home";
import Button from 'react-bootstrap/Button';

class Viewplaylists extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            items: [], // aici vor fi salvate playlist-urile
            isLoaded: false,
            buttonPressed: "",
            selectedPlaylist: "",
            selectedSong: "",
            selectedSongLink: ""
        };
        console.log(this.props);
        this.handleChange = this.handleChange.bind(this);
        this.handleChangePlaylist = this.handleChangePlaylist.bind(this);
    }

    setViewMusicDestination(message) {
        this.setState({ buttonPressed: message });
    }

    componentDidMount() {
        fetch(
            "http://localhost:8081/api/playlists/" + this.props.id + "?field=idUser",
            {
                method: 'get',
                mode: 'cors',
                headers: {
                    'Access-Control-Allow-Origin': '*',
                    'Authorization': 'Bearer ' + this.props.token
                }
            })
            .then((res) => {
                // console.log(res);
                return res.json()
            })
            .then((json) => {
                // console.log(json);
                let _embedded = json._embedded;
                let playlists = _embedded.outputPlaylistDTOList;
                
                this.setState({
                    items: playlists,
                    isLoaded: true
                });
            })
            .catch((error) => {
                alert("User-ul nu are playlist-uri!");
            });
    }

    getSongFromRest(selfLink, token) {
        return new Promise(function (resolve, reject) {
            let xmlHttp = new XMLHttpRequest();
            xmlHttp.open("GET", selfLink, true);

            // let responseMessage;
            xmlHttp.onreadystatechange = function () {
                if (xmlHttp.readyState === 4) {
                    if (xmlHttp.status === 200) {
                        resolve(xmlHttp.responseText);
                    }
                    else if (xmlHttp.status === 404) {
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
            xmlHttp.send();
        });
    }

    async getMoreDetailsAboutMusic(songName) {
        let song = this.state.selectedPlaylist.songs.filter(x => x.name === songName)[0];
        if (song !== undefined) {
            let selfLink = Object.entries(song).filter(x => x[0] === "selfLink")[0][1];

            this.getSongFromRest(selfLink, this.state.token)
                .then(
                    (res) => {
                        // console.log(res);
                        res = JSON.parse(res);
                        let name = res.name;
                        let genre = res.genre;
                        let type = res.type;
                        let artists = res.artist;
                        console.log(res);

                        let artistsName = "";
                        for (let artist of artists) {
                            artistsName += artist.name + "\n";
                        }
                        let info = "Name: " + name + "\nGenre: " + genre + "\nType: " + type + "\nArtists: " + artistsName;

                        document.getElementById("info_song").innerText = info;
                        document.getElementById("link_song").setAttribute('href', selfLink);
                        document.getElementById("link_song").innerText = "Go to song";
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
                });
        }
    }

    handleChange(event) {
        let value = event.target.value;
        if (value !== "") {
            this.setState({ selectedSong: value });
            this.getMoreDetailsAboutMusic(value);
        }
    }

    handleChangePlaylist(event) {
        let value = event.target.value;
        if (value !== "") {
            let playlist = this.state.items.filter(x => x.playlistName === value)[0];
            this.setState({ selectedPlaylist: playlist });
        }
    }


    render() {
        if (this.state.buttonPressed === "home") {
            return (
                <Home token={this.props.token}
                    username={this.props.username}
                    password={this.props.password}
                    id={this.props.id} />
            );
        }
        if (!this.state.isLoaded) {
            return (
                <>
                    <p> Asteptati sa se incarce playlist-urile. </p>
                    <Button className="button" onClick={(e) => this.setViewMusicDestination("home")}>Back</Button>
                </>
            )
        }
        else {
            // options for music 
            let options = [];
            options.push({ "value": "", label: "Select a song" });
            if (this.state.selectedPlaylist !== "") {
                let optionsSongs = this.state.selectedPlaylist.songs.map(song => song.name);
                optionsSongs = optionsSongs.map((x) => ({ "value": x, "label": x }));

                options.push(...optionsSongs);
                // console.log(this.state);
            }

            let optionsForPlaylists = this.state.items.map(playlist => playlist.playlistName);
            optionsForPlaylists = optionsForPlaylists.map((x) => ({ "value": x, "label": x }));
            let optionsPlaylist = [];
            optionsPlaylist.push({ "value": "", label: "Select a playlist" });
            optionsPlaylist.push(...optionsForPlaylists);
            // console.log(optionsPlaylist);

            return (
                <div className="App" >

                    <h1 > Playlists from Rest </h1>
                    <table className="login_text">
                        <thead>
                            <tr >
                                <th> Id User </th>
                                <th> Name </th>
                                <th> Visibility </th>
                            </tr >
                        </thead>
                        <tbody>
                        {

                            this.state.items.map((item) => (


                                <tr>
                                    <td key={item.idUser}> {item.idUser} </td>
                                    <td key={item.playlistName}> {item.playlistName} </td>
                                    <td key={item.visibility}> {item.visibility} </td>
                                </tr>
                            ))
                        }
                        </tbody>
                    </table>
                    <br></br>
                    <br></br>

                    <p className="login_text"> Select a playlist:</p>
                    <select onChange={this.handleChangePlaylist} className="login_text">
                        {
                            optionsPlaylist.map((option) => (
                                <option value={option.value}>{option.label}</option>
                            ))
                        }
                    </select>
                    <br></br>
                    <table >
                        <tr >
                            <th> Name </th>
                        </tr >
                        {
    
                            options.map((item) => (
                                <tr>
                                    <td > {item.value} </td>
                                </tr>
                            ))
                        }
                    </table>
                    <p className="login_text"> Select a song from list to see more details:</p>
                    <select onChange={this.handleChange} className="login_text">
                        {
                            options.map((option) => (
                                <option value={option.value}>{option.label}</option>
                            ))
                        }
                    </select>
                    <br></br>
                    <br></br>
                    <pre id="info_song" className="login_text"></pre>
                    <a href="" id="link_song"></a>
                    <br></br>
                    <Button className="button" onClick={(e) => this.setViewMusicDestination("home")}>Back</Button>
                </div >
            );
        }
    };
}

export default Viewplaylists;